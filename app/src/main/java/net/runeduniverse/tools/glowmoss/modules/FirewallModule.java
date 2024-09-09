/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.glowmoss.modules;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.runeduniverse.tools.glowmoss.ConsoleLogger;
import net.runeduniverse.tools.glowmoss.filter.IgnoreFilter;
import net.runeduniverse.tools.glowmoss.model.firewall.BaseChain;
import net.runeduniverse.tools.glowmoss.model.firewall.Chain;
import net.runeduniverse.tools.glowmoss.model.firewall.EgressHook;
import net.runeduniverse.tools.glowmoss.model.firewall.Firewall;
import net.runeduniverse.tools.glowmoss.model.firewall.Hook;
import net.runeduniverse.tools.glowmoss.model.firewall.IngressHook;
import net.runeduniverse.tools.glowmoss.model.firewall.Rule;
import net.runeduniverse.tools.glowmoss.model.firewall.Table;
import net.runeduniverse.tools.glowmoss.modules.firewall.FilteredTable;
import net.runeduniverse.tools.glowmoss.options.InvalidArgumentException;
import net.runeduniverse.tools.glowmoss.options.MatchOptions;
import net.runeduniverse.tools.glowmoss.options.MissingOptionException;
import net.runeduniverse.tools.glowmoss.options.Options;
import net.runeduniverse.tools.glowmoss.parser.FirewallParser;

public class FirewallModule implements ExecModule {

	private Options options;
	private boolean modeMatch = false;

	private final Set<Rule> ignoredRules = new LinkedHashSet<>();
	private final Set<Chain> chainsWithIgnoredRules = new LinkedHashSet<>();
	private final Map<Chain, String> simpleResults = new LinkedHashMap<>();

	@Override
	public boolean handle(ListIterator<String> argPtr) throws InvalidArgumentException {
		switch (argPtr.next()) {
		case "firewall":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Module is missing arguments! : [match|?]");
			}

			switch (argPtr.next()) {
			case "match":
				modeMatch = true;
				return true;
			}
			// give another module the chance to load an extension
			argPtr.previous();
		}
		argPtr.previous();
		return false;
	}

	@Override
	public void help(ConsoleLogger logger, Options options) {
		System.out.println(">> Glowmoss");

		if (this.modeMatch) {
			System.out.println("    firewall match --nft-ruleset <path/to/ruleset> --match-* <value> [...]");
			System.out.println("      optional:");
			// System.out.println(" --match-value <value>");
			System.out.println("        --match-rule-by-regex <regex>");
			System.out.println("        --match-hook-name     <name>");
			System.out.println("        --match-chain-type    <type>");
			System.out.println("        --match-family        <type>");
			System.out.println("        --show-dormant-tables");
			System.out.println("        --hide-empty-chains");
			System.out.println("        --ignore-ipv6-rules");
		} else {
			System.out.println("    firewall <mode>");
			System.out.println("      modes:");
			System.out.println("        match");
			System.out.println();
			System.out.println("    Help:");
			System.out.println("    $ glowmoss firewall [mode] --help");
		}

		System.out.println();
	}

	@Override
	public void validate(ConsoleLogger logger, Options options) throws MissingOptionException {
		options.nftOptions()
				.requireRuleset();
	}

	@Override
	public boolean exec(ConsoleLogger logger, Options options) {
		this.options = options;

		final Firewall firewall = Firewall.create();
		final Set<Table> tables = parseFW(firewall);
		if (tables == null) {
			logger.info("Failed to parse Firewall!");
			return false;
		}

		if (this.modeMatch)
			execMatch(firewall, tables, this.options.matchOptions());

		return true;
	}

	private Set<Table> parseFW(Firewall firewall) {
		final FirewallParser parser = new FirewallParser(firewall);
		final Path ruleset = this.options.nftOptions()
				.ruleset();
		if (ruleset == null)
			return null;

		try {
			parser.parse(ruleset);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		final Set<Table> tables = new LinkedHashSet<>();
		tables.addAll(parser.getResults());
		return tables;
	}

	private void execMatch(Firewall firewall, Set<Table> tables, MatchOptions options) {

		final boolean hideEmptyChains = options.hideEmptyChains();

		for (Table table : tables) {
			final FilteredTable fTable = new FilteredTable(table);
			if (!fTable.filter(options))
				continue;

			System.out.println("============================================================");
			System.out.println(String.format(" %s Table:  Name: %-10s Family: %-6s",
					table.getDormant() ? "[🗴]" : "[✓]", table.getName(), table.getFamily()
							.text()));

			final Map<Integer, Map<Integer, BaseChain>> tree = fTable.getTree();
			final Set<BaseChain> baseChainFilter = fTable.getMatchedBaseChains();
			final Set<Rule> matchedRules = fTable.getMatchedRules();
			final Set<Rule> includedRules = fTable.getMatchedRules();
			final LinkedList<Chain> selected = select(baseChainFilter, matchedRules, includedRules, hideEmptyChains);

			for (Chain chain : selected) {
				printChain(chain, chain.getRules(), new LinkedHashSet<>(selected), includedRules);
			}

		}
	}

	protected LinkedList<Chain> select(final Set<BaseChain> baseChainFilter, final Set<Rule> matchedRules,
			final Set<Rule> includedRules, final boolean hideEmptyChains) {
		final Set<Chain> excluded = new LinkedHashSet<>();
		final LinkedList<Chain> result = new LinkedList<>();

		final Set<Chain> chains = new LinkedHashSet<>();
		for (Rule rule : matchedRules) {
			if (keepRuleCheck(rule))
				chains.add(rule.getChain());
		}

		final Set<Chain> valid = new LinkedHashSet<>();
		final Set<Chain> flagged = new LinkedHashSet<>();

		for (Chain chain : chains)
			revSearch(baseChainFilter, excluded, result, includedRules, chain);

		{
			final Set<Chain> resultSet = new LinkedHashSet<>(result);
			for (Chain chain : chains) {
				if (fwdSearch(false, excluded, hideEmptyChains, resultSet, result, includedRules, chain))
					valid.add(chain);
				else
					flagged.add(chain);
			}
		}

		if (hideEmptyChains) {
			for (Chain chain : flagged)
				purgeResult(result, valid, chain);
			// truncate simple results!
			final Set<Chain> resultSet = new LinkedHashSet<Chain>(result);
			for (Chain chain : resultSet) {
				computeSimpleResult(resultSet, chain);
			}

			for (Iterator<Chain> i = result.descendingIterator(); i.hasNext();) {
				final Chain c = i.next();
				String s = getSimpleResult(resultSet, c);
				if (s != null) {
					i.remove();
				}
			}
		}

		return result;
	}

	protected boolean fwdSearch(final boolean checkContains, final Set<Chain> excluded, final boolean hideEmptyChains,
			final Set<Chain> resultSet, final LinkedList<Chain> result, final Set<Rule> includedRules,
			final Chain chain) {
		if (chain == null || excluded.contains(chain))
			return false;
		if (checkContains && resultSet.contains(chain))
			return true;

		final LinkedList<Chain> tmpResult = new LinkedList<>();
		final Set<Chain> tmpResultSet = new LinkedHashSet<>(resultSet);

		boolean matched = false;
		for (Rule rule : chain.getRules()) {
			final LinkedList<Chain> partialResult = new LinkedList<>();
			final Set<Chain> nextResultSet = new LinkedHashSet<>(tmpResultSet);

			if (!keepRuleCheck(rule)) {
				this.chainsWithIgnoredRules.add(chain);
				continue;
			}

			if (!rule.hasTargetRef()) {
				matched = true;
				includedRules.add(rule);
				continue;
			}

			boolean jumpMatch = fwdSearch(true, excluded, hideEmptyChains, nextResultSet, partialResult, includedRules,
					rule.getJumpTo());

			boolean gotoMatch = fwdSearch(true, excluded, hideEmptyChains, nextResultSet, partialResult, includedRules,
					rule.getGoTo());

			if (jumpMatch || gotoMatch) {
				matched = true;
				tmpResult.addAll(partialResult);
				tmpResultSet.addAll(nextResultSet);
				includedRules.add(rule);
			}
		}

		if (matched) {
			tmpResultSet.add(chain);
		} else if (hideEmptyChains) {
			excluded.add(chain);
			return false;
		}

		if (checkContains)
			tmpResult.addFirst(chain);
		resultSet.addAll(tmpResultSet);
		result.addAll(tmpResult);
		return true;
	}

	protected boolean revSearch(final Set<BaseChain> baseChainFilter, final Set<Chain> excluded,
			final LinkedList<Chain> result, final Set<Rule> includedRules, final Chain chain) {
		if (excluded.contains(chain))
			return false;
		if (result.contains(chain)) {
			return true;
		}
		if (chain instanceof BaseChain) {
			final BaseChain baseChain = (BaseChain) chain;
			if (baseChainFilter.contains(baseChain)) {
				result.add(baseChain);
				return true;
			}
			excluded.add(baseChain);
			return false;
		}

		boolean matched = false;
		for (Rule rule : chain.getJumpSources()) {
			if (keepRuleCheck(rule)) {
				if (revSearch(baseChainFilter, excluded, result, includedRules, rule.getChain())) {
					matched = true;
					includedRules.add(rule);
				}
			} else {
				this.chainsWithIgnoredRules.add(rule.getChain());
			}
		}
		if (!matched) {
			for (Rule rule : chain.getGotoSources())
				if (keepRuleCheck(rule)) {
					if (revSearch(baseChainFilter, excluded, result, includedRules, rule.getChain())) {
						matched = true;
						includedRules.add(rule);
					}
				} else {
					this.chainsWithIgnoredRules.add(rule.getChain());
				}
		}

		if (matched) {
			result.add(chain);
			return true;
		}

		excluded.add(chain);
		return false;
	}

	protected void purgeResult(final LinkedList<Chain> result, final Set<Chain> valid, final Chain chain) {
		if (chain == null || valid.contains(chain) || !result.contains(chain))
			return;

		for (Rule rule : chain.getRules()) {
			Chain targetChain = rule.getJumpTo();
			if (targetChain == null)
				targetChain = rule.getGoTo();
			if (targetChain == null)
				continue;
			// if the chain has a downstream valid chain it is also becomes valid
			if (valid.contains(targetChain)) {
				valid.add(chain);
				return;
			}
		}

		// purge chain
		result.remove(chain);

		// try to purge upstream chains
		for (Rule rule : chain.getJumpSources())
			purgeResult(result, valid, rule.getChain());
		for (Rule rule : chain.getGotoSources())
			purgeResult(result, valid, rule.getChain());
	}

	protected void printChain(final Chain chain, final Collection<Rule> rules, final Set<Chain> includedChains,
			final Set<Rule> includedRules) {
		System.out.println("    Chain: " + chain.getName());

		if (chain instanceof BaseChain) {
			final BaseChain baseChain = (BaseChain) chain;

			final Hook hook = baseChain.getHook();
			String sHook = hook.getName();
			if (hook instanceof IngressHook || hook instanceof EgressHook) {
				sHook = sHook + "[device: " + baseChain.getDevice() + "]";
			}
			String policy = baseChain.getPolicy();
			if (policy == null)
				policy = "(default)";
			System.out.println(String.format("    ⚓︎ Hook: %-20s Policy: %9s Priority: [%4d] %-20s", sHook, policy,
					baseChain.getEffPriority(), baseChain.getPriority()));

		}

		boolean hadIncluded = false;
		boolean skip = false;
		String line = null;
		String note = null;
		for (Rule rule : rules) {
			// only show generic hits before the selected rules
			if (includedRules.contains(rule)) {
				hadIncluded = true;
			} else if (hadIncluded) {
				continue;
			}

			if (keepRuleCheck(rule)) {
				// has target?
				Chain targetChain = rule.getJumpTo();
				if (targetChain == null)
					targetChain = rule.getGoTo();
				if (targetChain == null) {
					skip = false;
				} else {
					if (includedChains.contains(targetChain))
						skip = false;
					else if ((note = this.simpleResults.get(targetChain)) != null) {
						skip = false;
					} else {
						if (skip)
							continue;
						skip = true;
					}
				}

			} else {
				// rule => ignored
				skip = true;
			}

			if (line != null) {
				System.out.println(line);
				if (note != null) {
					System.out.println("      |   >> " + note);
					note = null;
				}
			}
			line = skip ? "      ¦ <hidden>" : "      | " + rule.getContent();
		}
		if (!skip && line != null) {
			System.out.println(line);
			if (note != null)
				System.out.println("          >> " + note);
		}
	}

	protected void computeSimpleResult(final Set<Chain> selection, final Chain chain) {
		if (this.simpleResults.containsKey(chain))
			return;

		final Iterator<Rule> i = chain.getRules()
				.iterator();
		Rule rule = null;
		String content = null;
		// forward to relevant rule
		while (i.hasNext()) {
			rule = (Rule) i.next();
			content = rule.getContent();
			if (keepRuleCheck(rule) && !content.startsWith("jump"))
				break;
		}
		if (rule == null) {
			// no relevant rules!
			this.simpleResults.put(chain, null);
			return;
		}

		if (content.startsWith("accept") || content.startsWith("drop") || content.startsWith("reject")) {
			// content is already set!
		} else {
			return;
		}

		// check for another rule, if the matched rule isn't last something is off!
		// => log everything!
		this.simpleResults.put(chain, i.hasNext() ? null : content);
	}

	protected String getSimpleResult(final Set<Chain> selection, final Chain chain) {
		if (this.simpleResults.containsKey(chain))
			return this.simpleResults.get(chain);

		final Iterator<Rule> i = chain.getRules()
				.iterator();
		Rule rule = null;
		String content = null;
		// forward to relevant rule
		while (i.hasNext()) {
			rule = (Rule) i.next();
			content = rule.getContent();

			if (keepRuleCheck(rule)) {
				final Chain jumpTarget = rule.getJumpTo();
				if (jumpTarget != null && content.startsWith("jump")) {
					content = this.simpleResults.get(jumpTarget);
					if (content == null)
						continue;
				}
				break;
			}
		}

		if (rule == null) {
			// no relevant rules!
			return null;
		}

		final Chain gotoTarget = rule.getGoTo();
		if (gotoTarget != null && content.startsWith("goto")) {
			content = this.simpleResults.get(gotoTarget);
			// if (content == null)
			// content = getPolicyNote(selection, chain);
			// System.err.println("simple + pol " + content);
		} else
			content = null;

		// check for another rule, if the matched rule isn't last something is off!
		// => log everything!
		content = i.hasNext() ? null : content;
		this.simpleResults.put(chain, content);
		return content;
	}

	protected String getPolicyNote(final Set<Chain> selection, final Chain chain) {
		final Set<BaseChain> roots = new LinkedHashSet<>();
		collectRoots(roots, new LinkedHashSet<>(), selection, chain);
		final List<String> lst = new LinkedList<>();
		for (BaseChain r : roots) {
			final String policy = r.getPolicy();
			lst.add(String.format("%s : %s", r.getName(), policy == null ? "(default policy)" : policy));
		}
		return "{ " + String.join(", ", lst) + " }";
	}

	protected void collectRoots(final Set<BaseChain> roots, final Set<Chain> paths, final Set<Chain> selection,
			final Chain chain) {
		if (paths.contains(chain) || !selection.contains(chain))
			return;
		paths.add(chain);

		if (chain instanceof BaseChain) {
			roots.add((BaseChain) chain);
			return;
		}

		final Set<Chain> sources = new LinkedHashSet<>();
		for (Rule rule : chain.getJumpSources())
			sources.add(rule.getChain());
		for (Rule rule : chain.getGotoSources())
			sources.add(rule.getChain());

		for (Chain c : sources)
			collectRoots(roots, paths, selection, c);
	}

	protected boolean ignoreRuleCheck(final Rule rule) {
		if (this.ignoredRules.contains(rule))
			return true;

		final String content = rule.getContent();
		final MatchOptions options = this.options.matchOptions();

		if (options.ignoreIpv6Rules() && (content.startsWith("ip6") || content.startsWith("icmpv6")) //
		) {
			this.ignoredRules.add(rule);
			return true;
		}

		return false;
	}

	protected boolean keepRuleCheck(final Rule rule) {
		return !ignoreRuleCheck(rule);
	}

}
