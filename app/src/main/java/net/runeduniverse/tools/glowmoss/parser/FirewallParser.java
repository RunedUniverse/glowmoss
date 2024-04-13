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
package net.runeduniverse.tools.glowmoss.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.runeduniverse.tools.glowmoss.model.firewall.BaseChain;
import net.runeduniverse.tools.glowmoss.model.firewall.Chain;
import net.runeduniverse.tools.glowmoss.model.firewall.ChainType;
import net.runeduniverse.tools.glowmoss.model.firewall.Family;
import net.runeduniverse.tools.glowmoss.model.firewall.Firewall;
import net.runeduniverse.tools.glowmoss.model.firewall.Rule;
import net.runeduniverse.tools.glowmoss.model.firewall.Table;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@RequiredArgsConstructor
public class FirewallParser {

	@Getter
	protected Set<Table> results = new LinkedHashSet<>();
	protected Map<Family, Map<String, TmpTable>> tables = new LinkedHashMap<>();

	protected final Firewall firewall;

	public void parse(Path path) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				String[] arr = line.split(" ");
				if (arr.length == 4 && //
						"table".equals(arr[0]) && //
						"{".equals(arr[3]) && //
						parseTable(reader, Family.find(arr[1]), arr[2]))
					continue;
				if (arr.length == 3 && //
						"table".equals(arr[0]) && //
						"{".equals(arr[2]) && //
						parseTable(reader, Table.DEFAULT_FAMILY, arr[1]))
					continue;
				// match other combos ...
			}
		}
	}

	protected boolean parseTable(final BufferedReader reader, Family family, String name) throws IOException {
		if (family == null || isBlank(name))
			return false;

		TmpTable tempTable = acquireTable(family, name);

		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			line = line.trim();
			if ("}".equals(line)) {
				this.results.add(tempTable.compile());
				return true;
			}
			if (line.isEmpty())
				continue;

			String[] arr = line.split(" ");

			if (arr.length == 3) {
				switch (arr[0]) {
				case "chain":
					parseChain(reader, tempTable, arr[1]);
					continue;
				case "set":
					parseSet(reader, tempTable, arr[1]);
					continue;
				}
			}
			parseStatefulObject(reader, tempTable, line);
		}

		return true;
	}

	protected boolean parseChain(final BufferedReader reader, final TmpTable tmpTable, String name) throws IOException {
		TmpChain tmpChain = tmpTable.getChains()
				.get(name);
		if (tmpChain == null) {
			tmpTable.getChains()
					.put(name, tmpChain = new TmpChain());
		}
		List<String> rawRules = tmpChain.getRawRules();
		boolean parseHeader = true;
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			line = line.trim();
			if ("}".equals(line))
				break;
			if (line.isEmpty())
				continue;

			if (parseHeader) {
				parseHeader = false;
				if (initFwChain(tmpTable.getTable()
						.getFamily(), tmpChain, name, line))
					continue;
			}
			rawRules.add(line);
		}
		if (parseHeader) {
			// init chain in case the chain has no rules!
			tmpChain.setChain(new Chain().setName(name));
		}
		return true;
	}

	protected boolean initFwChain(final Family family, final TmpChain tmpChain, final String name, final String line) {
		int state = 0;
		String type = null;
		String hook = null;
		List<String> priority = new LinkedList<>();
		List<String> policy = new LinkedList<>();
		for (String s : line.split(" ")) {
			if (isBlank(s))
				continue;
			switch (state) {
			case 0:
				// determine new state
				switch (s) {
				case "type":
					state = 1;
					continue;
				case "hook":
					state = 2;
					continue;
				case "priority":
					state = 3;
					continue;
				case "policy":
					state = 4;
					continue;
				}
				break;
			case 1:
				type = s;
				state = 0;
				continue;
			case 2:
				hook = s;
				state = 0;
				continue;
			case 3:
				int idx0 = s.indexOf(';');
				if (idx0 == -1) {
					priority.add(s);
					continue;
				}
				if (idx0 != 0)
					priority.add(s.substring(0, idx0));
				state = 0;
				continue;
			case 4:
				int idx1 = s.indexOf(';');
				if (idx1 == -1) {
					policy.add(s);
					continue;
				}
				if (idx1 != 0)
					policy.add(s.substring(0, idx1));
				state = 0;
				continue;
			}
		}
		Chain chain;
		boolean found = false;
		if (type != null && hook != null && !priority.isEmpty()) {
			// BaseChain
			chain = new BaseChain().setType(ChainType.find(type))
					.setHook(this.firewall.findHook(family, hook))
					.setPriority(String.join(" ", priority))
					.setPolicy(String.join(" ", policy));
			found = true;
		} else {
			// Chain
			chain = new Chain();
		}
		chain.setName(name);
		tmpChain.setChain(chain);
		return found;
	}

	protected boolean parseSet(final BufferedReader reader, final TmpTable tempTable, String name) throws IOException {
		final StringBuilder content = new StringBuilder();
		// header
		content.append("set ")
				.append(name)
				.append(" {");
		// text
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			line = line.trim();
			if ("}".equals(line)) {
				// end
				content.append("\n}");
				break;
			}
			if (line.isEmpty())
				continue;
			content.append("\n\t")
					.append(line);
		}
		tempTable.getSets()
				.add(content.toString());
		return true;
	}

	protected boolean parseStatefulObject(final BufferedReader reader, final TmpTable tempTable, String line0)
			throws IOException {
		final StringBuilder content = new StringBuilder();
		// header
		content.append(line0);
		// text
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			line = line.trim();
			if ("}".equals(line)) {
				// end
				content.append("\n}");
				break;
			}
			if (line.isEmpty())
				continue;
			content.append("\n\t")
					.append(line);
		}
		tempTable.getStatefulObjects()
				.add(content.toString());
		return true;
	}

	protected TmpTable acquireTable(Family family, String name) {
		Map<String, TmpTable> map = this.tables.get(family);
		if (map == null) {
			this.tables.put(family, map = new LinkedHashMap<>());
		}
		TmpTable tmp = map.get(name);
		if (tmp != null) {
			return tmp;
		}
		map.put(name, tmp = new TmpTable().setTable(new Table().setFamily(family)
				.setName(name)));
		return tmp;
	}

	@Data
	@Accessors(chain = true)
	public static class TmpTable {

		private final Map<String, TmpChain> chains = new LinkedHashMap<>();

		private final List<String> sets = new LinkedList<>();

		private final List<String> statefulObjects = new LinkedList<>();

		private Table table;

		public Table compile() {
			for (TmpChain tmp : this.chains.values()) {
				this.table.addChain(tmp.compile(this));
			}
			this.table.getSets()
					.addAll(this.sets);
			this.table.getStatefulObjects()
					.addAll(this.statefulObjects);
			return this.table;
		}

		public Chain findFwChain(final String name) {
			if (name == null)
				return null;
			final TmpChain tmp = this.chains.get(name);
			if (tmp == null)
				return null;
			return tmp.getChain();
		}
	}

	@Data
	@Accessors(chain = true)
	public static class TmpChain {

		private final List<String> rawRules = new LinkedList<>();

		private Chain chain;

		public Chain compile(TmpTable tmpTable) {
			// parse jump / goto in rules
			for (String rawRule : this.rawRules) {
				int state = 0;
				String sGoto = null;
				String sJump = null;
				lineLoop: for (String s : rawRule.split(" ")) {
					if (isBlank(s))
						continue;
					switch (state) {
					case 0:
						switch (s) {
						case "goto":
							state = 1;
							continue;
						case "jump":
							state = 2;
							continue;
						}
					default:
						break;
					// only 1 per rule
					case 1:
						sGoto = s;
						break lineLoop;
					case 2:
						sJump = s;
						break lineLoop;
					}
				}
				this.chain.addRule(new Rule().setContent(rawRule)
						.setGoTo(tmpTable.findFwChain(sGoto))
						.setJumpTo(tmpTable.findFwChain(sJump)));
			}
			return this.chain;
		}
	}
}
