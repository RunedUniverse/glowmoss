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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import net.runeduniverse.tools.glowmoss.ConsoleLogger;
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

	private final Set<Rule> hiddenRules = new LinkedHashSet<>();
	private final Set<Chain> chainsWithHiddenRules = new LinkedHashSet<>();

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
		final Set<Chain> resultSet = new LinkedHashSet<>(result);
		for (Chain chain : chains) {
			if (fwdSearch(false, excluded, hideEmptyChains, resultSet, result, includedRules, chain))
				valid.add(chain);
			else
				flagged.add(chain);
		}

		if (hideEmptyChains) {
			for (Chain chain : flagged)
				purgeResult(result, valid, chain);
		}

		return result;
	}

	protected boolean keepRuleCheck(final Rule rule) {
		if (this.hiddenRules.contains(rule))
			return false;

		final String content = rule.getContent();
		final MatchOptions options = this.options.matchOptions();

		if (!(options.ignoreIpv6Rules() && (content.startsWith("ip6") || content.startsWith("icmpv6"))))
			return true;

		this.hiddenRules.add(rule);
		return false;
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
				this.chainsWithHiddenRules.add(chain);
				continue;
			}

			if (!rule.hasTargetRef()) {
				matched = true;
				includedRules.add(rule);
				continue;
			}

			if (fwdSearch(true, excluded, hideEmptyChains, nextResultSet, partialResult, includedRules,
					rule.getJumpTo()) || //
					fwdSearch(true, excluded, hideEmptyChains, nextResultSet, partialResult, includedRules,
							rule.getGoTo())) {
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
				this.chainsWithHiddenRules.add(rule.getChain());
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
					this.chainsWithHiddenRules.add(rule.getChain());
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
					else {
						if (skip)
							continue;
						skip = true;
					}
				}

			} else {
				// rule => ignored
				skip = true;
			}

			if (line != null)
				System.out.println(line);
			line = skip ? "      ¦ <hidden>" : "      | " + rule.getContent();
		}
		if (!skip && line != null)
			System.out.println(line);
	}

	@FunctionalInterface
	protected static interface RuleFilter {

		public boolean check(final Rule rule);
	}

}
