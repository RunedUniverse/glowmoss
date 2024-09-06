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

import java.util.ListIterator;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.runeduniverse.lib.rogm.Configuration;
import net.runeduniverse.lib.rogm.modules.neo4j.Neo4jConfiguration;
import net.runeduniverse.lib.utils.common.StringUtils;

@Accessors(fluent = true)
@Getter
public class DBOptions {

	// DB: Neo4j
	private String dbAddress = null;
	// Protocol: bolt | Port: 7687
	private int dbPort = 7687;
	private String dbUser = "neo4j";
	private String dbPassword = "glowmoss";

	public boolean handle(ListIterator<String> argPtr) throws InvalidArgumentException {
		switch (argPtr.next()) {
		case "--db-addr":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --db-addr <address>");
			}
			this.dbAddress = argPtr.next();
			return true;

		case "--db-port":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --db-port <port>");
			}
			String val = argPtr.next();
			int p = -1;
			try {
				p = Integer.parseInt(val, 10);
			} catch (Exception e) {
			}
			if (p < 1 || 65535 < p) {
				throw new InvalidArgumentException("Invalid database port: " + val);
			}
			this.dbPort = p;
			return true;

		case "--db-user":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --db-user <user>");
			}
			this.dbUser = argPtr.next();
			return true;

		case "--db-pass":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --db-pass <password>");
			}
			this.dbPassword = argPtr.next();
			return true;
		}
		// reset prt if no match was found!
		argPtr.previous();
		return false;
	}

	public DBOptions requireAddress() throws MissingOptionException {
		// eval required args
		if (StringUtils.isBlank(this.dbAddress)) {
			throw new MissingOptionException("Missing value: --db-addr <address>");
		}
		return this;
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
}
