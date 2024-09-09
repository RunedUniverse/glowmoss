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
package net.runeduniverse.tools.glowmoss.modules.firewall;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runeduniverse.tools.glowmoss.model.firewall.BaseChain;
import net.runeduniverse.tools.glowmoss.model.firewall.Chain;
import net.runeduniverse.tools.glowmoss.model.firewall.ChainType;
import net.runeduniverse.tools.glowmoss.model.firewall.Family;
import net.runeduniverse.tools.glowmoss.model.firewall.Firewall;
import net.runeduniverse.tools.glowmoss.model.firewall.Hook;
import net.runeduniverse.tools.glowmoss.model.firewall.Rule;
import net.runeduniverse.tools.glowmoss.model.firewall.Table;
import net.runeduniverse.tools.glowmoss.options.MatchOptions;

@RequiredArgsConstructor
public class FilteredTable {

	@Getter
	private final Table table;
	@Getter
	private final Map<Integer, Map<Integer, BaseChain>> tree = new TreeMap<>();
	@Getter
	private final Set<BaseChain> matchedBaseChains = new LinkedHashSet<>();
	@Getter
	private final Set<Rule> matchedRules = new LinkedHashSet<>();

	public boolean filter(final MatchOptions options) {

		final Set<String> matchRuleByRegex = options.matchRuleByRegex();
		final Set<String> matchHookName = options.matchHookName();
		final Set<ChainType> matchChainType = options.matchChainType();
		final Set<Family> matchFamily = options.matchFamily();
		final boolean hideDormantTables = !options.showDormantTables();

		if (table.getDormant() && hideDormantTables)
			return false;

		if (!matchFamily.isEmpty() && !matchFamily.contains(table.getFamily())) {
			return false;
		}

		for (Chain chain : table.getChains()) {
			if (chain instanceof BaseChain) {
				final BaseChain baseChain = (BaseChain) chain;
				if (!matchChainType.isEmpty() && !matchChainType.contains(baseChain.getType()))
					continue;

				final Hook hook = baseChain.getHook();
				if (!matchHookName.isEmpty() && !matchHookName.contains(hook.getName()))
					continue;

				this.matchedBaseChains.add(baseChain);

				final Integer idx0 = Firewall.hookToSortIndex(hook);
				Map<Integer, BaseChain> subTree = tree.get(idx0);
				if (subTree == null)
					tree.put(idx0, subTree = new TreeMap<>());
				subTree.put(baseChain.getEffPriority(), baseChain);
			}

			if (matchRuleByRegex.isEmpty())
				this.matchedRules.addAll(chain.getRules());
			else
				for (Rule rule : chain.getRules()) {
					final String content = rule.getContent();
					for (String regex : matchRuleByRegex)
						if (content.matches(regex)) {
							this.matchedRules.add(rule);
							break;
						}
				}
		}

		return true;
	}

}
