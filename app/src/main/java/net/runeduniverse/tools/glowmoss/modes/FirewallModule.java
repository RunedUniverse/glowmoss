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
package net.runeduniverse.tools.glowmoss.modes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.Getter;
import net.runeduniverse.tools.glowmoss.ConsoleLogger;
import net.runeduniverse.tools.glowmoss.model.firewall.BaseChain;
import net.runeduniverse.tools.glowmoss.model.firewall.Chain;
import net.runeduniverse.tools.glowmoss.model.firewall.ChainType;
import net.runeduniverse.tools.glowmoss.model.firewall.EgressHook;
import net.runeduniverse.tools.glowmoss.model.firewall.Family;
import net.runeduniverse.tools.glowmoss.model.firewall.Firewall;
import net.runeduniverse.tools.glowmoss.model.firewall.Hook;
import net.runeduniverse.tools.glowmoss.model.firewall.IngressHook;
import net.runeduniverse.tools.glowmoss.model.firewall.Rule;
import net.runeduniverse.tools.glowmoss.model.firewall.Table;
import net.runeduniverse.tools.glowmoss.modes.firewall.FilteredTable;
import net.runeduniverse.tools.glowmoss.options.InvalidArgumentException;
import net.runeduniverse.tools.glowmoss.options.MatchOptions;
import net.runeduniverse.tools.glowmoss.options.MissingOptionException;
import net.runeduniverse.tools.glowmoss.options.Options;
import net.runeduniverse.tools.glowmoss.parser.FirewallParser;

public class FirewallModule implements ExecModule {

	private Options options;
	private boolean modeMatch = false;

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
		System.out.println("    firewall match --nft-ruleset <path/to/ruleset> --match-* <value> [...]");
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

		final Set<String> matchRuleByRegex = options.matchRuleByRegex();
		final Set<String> matchHookName = options.matchHookName();
		final Set<ChainType> matchChainType = options.matchChainType();
		final Set<Family> matchFamily = options.matchFamily();
		final boolean hideDormantTables = !options.showDormantTables();
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
			final LinkedList<Chain> selected = select(baseChainFilter, matchedRules, includedRules);

			// for (Map.Entry<Integer, Map<Integer, BaseChain>> entry : tree.entrySet()) {
			// for (BaseChain chain : entry.getValue()
			// .values()) {
			// if (hitBaseChains.contains(chain))
			// printChain(chain, chain.getRules(), includedRules);
			// }
			// }

			for (Chain chain : selected) {
				printChain(chain, chain.getRules(), includedRules);
			}

		}

	}

	protected LinkedList<Chain> select(final Set<BaseChain> baseChainFilter, final Set<Rule> matchedRules,
			final Set<Rule> includedRules) {
		final Set<Chain> chains = new LinkedHashSet<>();
		for (Rule rule : matchedRules)
			chains.add(rule.getChain());

		final Set<Chain> excluded = new LinkedHashSet<>();
		final LinkedList<Chain> result = new LinkedList<>();

		for (Chain chain : chains)
			revSearch(baseChainFilter, excluded, result, includedRules, chain);
		return result;
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
		for (Rule rule : chain.getJumpSources())
			if (revSearch(baseChainFilter, excluded, result, includedRules, rule.getChain())) {
				matched = true;
				includedRules.add(rule);
			}
		if (!matched)
			for (Rule rule : chain.getGotoSources())
				if (revSearch(baseChainFilter, excluded, result, includedRules, rule.getChain())) {
					matched = true;
					includedRules.add(rule);
				}

		if (matched) {
			result.add(chain);
			return true;
		}

		excluded.add(chain);
		return false;
	}

	protected void printChain(final Chain chain, final Collection<Rule> rules, final Set<Rule> included) {
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
		for (Rule rule : rules) {
			// only show generic hits before the selected rules
			if (included.contains(rule)) {
				hadIncluded = true;
			} else if (hadIncluded) {
				continue;
			}
			System.out.println("      | " + rule.getContent());
		}
	}

}
