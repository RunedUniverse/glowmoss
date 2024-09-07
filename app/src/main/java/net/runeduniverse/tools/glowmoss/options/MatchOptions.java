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
package net.runeduniverse.tools.glowmoss.options;

import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.runeduniverse.tools.glowmoss.model.firewall.ChainType;
import net.runeduniverse.tools.glowmoss.model.firewall.Family;

@Accessors(fluent = true)
@Getter
public class MatchOptions {

	private final Set<String> matchValues = new LinkedHashSet<>();
	private final Set<String> matchRuleByRegex = new LinkedHashSet<>();
	private final Set<String> matchHookName = new LinkedHashSet<>();
	private final Set<ChainType> matchChainType = new LinkedHashSet<>();
	private final Set<Family> matchFamily = new LinkedHashSet<>();
	private boolean showDormantTables = false;
	private boolean hideEmptyChains = false;

	public boolean handle(ListIterator<String> argPtr) throws InvalidArgumentException {
		String val = null;
		switch (argPtr.next()) {

		case "--match-value":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --match-value <value>");
			}
			this.matchValues.add(argPtr.next());
			return true;

		case "--match-rule-by-regex":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --match-rule-by-regex <regex>");
			}
			this.matchRuleByRegex.add(argPtr.next());
			return true;

		case "--match-hook-name":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --match-hook-name <name>");
			}
			this.matchHookName.add(argPtr.next());
			return true;

		case "--match-chain-type":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --match-chain-type <type>");
			}
			val = argPtr.next();
			final ChainType type = ChainType.find(val);
			if (type == null) {
				throw new InvalidArgumentException("Invalid value: --match-chain-type " + val);
			}
			this.matchChainType.add(type);
			return true;

		case "--match-family":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --match-family <type>");
			}
			val = argPtr.next();
			final Family family = Family.find(val);
			if (family == null) {
				throw new InvalidArgumentException("Invalid value: --match-family " + val);
			}
			this.matchFamily.add(family);
			return true;

		case "--show-dormant-tables":
			this.showDormantTables = true;
			return true;
		case "--hide-empty-chains":
			this.hideEmptyChains = true;
			return true;

		}
		// reset prt if no match was found!
		argPtr.previous();
		return false;
	}
}
