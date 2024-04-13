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
package net.runeduniverse.tools.glowmoss;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.runeduniverse.lib.rogm.Configuration;
import net.runeduniverse.lib.rogm.modules.neo4j.Neo4jConfiguration;
import net.runeduniverse.lib.utils.common.StringUtils;

@Accessors(fluent = true)
public class Options {

	// GENERAL
	@Getter
	private boolean log = false;
	@Getter
	private boolean debug = false;
	// DB: Neo4j
	private String dbAddress = null;
	// Protocol: bolt | Port: 7687
	private int dbPort = 7687;
	private String dbUser = "neo4j";
	private String dbPassword = "glowmoss";
	// PARSER: NFTables
	@Getter
	private Path nftRuleset = null;

	public boolean init(String[] argArr) {
		for (ListIterator<String> it = collectArgs(argArr).listIterator(); it.hasNext();) {
			String key = it.next();
			String val = null;

			switch (key) {
			case "--log":
				this.log = true;
				break;
			case "--debug":
				this.debug = true;
				break;
			case "--db-addr":
				if (!it.hasNext()) {
					System.err.println("ERR: Missing value: --db-addr <address>");
					return false;
				}
				this.dbAddress = it.next();
				break;
			case "--db-port":
				if (!it.hasNext()) {
					System.err.println("ERR: Missing value: --db-port <port>");
					return false;
				}
				val = it.next();
				int p = -1;
				try {
					p = Integer.parseInt(val, 10);
				} catch (Exception e) {
				}
				if (p < 1 || 65535 < p) {
					System.err.println("ERR: Invalid database port: " + val);
					return false;
				}
				break;
			case "--db-user":
				if (!it.hasNext()) {
					System.err.println("ERR: Missing value: --db-user <user>");
					return false;
				}
				this.dbUser = it.next();
				break;
			case "--db-pass":
				if (!it.hasNext()) {
					System.err.println("ERR: Missing value: --db-pass <password>");
					return false;
				}
				this.dbPassword = it.next();
				break;
			case "--nft-ruleset":
				if (!it.hasNext()) {
					System.err.println("ERR: Missing value: --nft-ruleset <path/to/ruleset>");
					return false;
				}
				val = it.next();
				try {
					this.nftRuleset = Paths.get(val);
				} catch (InvalidPathException e) {
					System.err.println("ERR: Invalid nft ruleset path: " + val);
					return false;
				}
				break;

			default:
				break;
			}
		}

		// eval required args
		if (StringUtils.isBlank(this.dbAddress)) {
			System.err.println("ERR: Missing value: --db-addr <address>");
			return false;
		}
		return true;
	}

	public Configuration dbConfig() {
		Neo4jConfiguration dbCnf = new Neo4jConfiguration(this.dbAddress);
		dbCnf.setPort(this.dbPort);
		// register model package
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model");
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.firewall");
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.firewall.app");
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.firewall.arp");
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.firewall.bridge");
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.firewall.ip");
		// dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.network");
		// dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.server");
		// dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.server.rel");
		// set classloader
		dbCnf.addClassLoader(getClass().getClassLoader());
		// set credentials
		dbCnf.setUser(this.dbUser);
		dbCnf.setPassword(this.dbPassword);
		return dbCnf;
	}

	protected List<String> collectArgs(String[] argArr) {
		final List<String> entries = new LinkedList<>();
		StringBuilder entry = null;
		boolean txtActive = false;
		for (Iterator<String> it = Arrays.asList(argArr)
				.iterator(); it.hasNext();) {
			String arg = it.next();
			if (entry == null)
				entry = new StringBuilder();

			if (txtActive) {
				// append space removed by splitting
				entry.append(' ');
			} else {
				// skip blanks
				if (StringUtils.isBlank(arg))
					continue;
			}

			entry.append(arg);

			for (int idxQuote = arg.indexOf('"'); -1 < idxQuote; idxQuote = arg.indexOf('"', idxQuote + 1)) {
				if (countIdentCharBeforeIdx(arg, '\\', idxQuote) % 2 == 0) {
					// quote is active => switch state
					txtActive = !txtActive;
				}
			}

			if (!txtActive) {
				entries.add(entry.toString());
				entry = null;
			}
		}
		if (entry != null && entry.length() != 0) {
			entries.add(entry.toString());
		}
		return entries;
	}

	protected int countIdentCharBeforeIdx(CharSequence txt, char c, int idx) {
		int cnt = 0;
		if (idx < 1 || txt == null || txt.length() < idx)
			return cnt;
		for (int i = idx - 1; 0 <= i; i = i - 1) {
			if (txt.charAt(i) == c)
				cnt = cnt + 1;
			else
				break;
		}
		return cnt;
	}

}
