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

import net.runeduniverse.lib.rogm.Configuration;
import net.runeduniverse.lib.rogm.Session;
import net.runeduniverse.lib.rogm.modules.neo4j.Neo4jConfiguration;
import net.runeduniverse.lib.rogm.querying.QueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.NodeQueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.RelationQueryBuilder;
import net.runeduniverse.tools.glowmoss.model.firewall.Chain;
import net.runeduniverse.tools.glowmoss.model.server.Host;

public class Launcher {

	private static Configuration dbCnf;
	private static QueryBuilder qryBuilder;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		dbCnf = configureDB();

		try (Session dbSession = Session.create(dbCnf)) {
			qryBuilder = dbSession.getQueryBuilder();

			initHost(dbSession);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initHost(Session session) {
		Host host = getHost(session, "rocky-vm");

		Chain postRt = new Chain();
		postRt.setName("POSTROUTING");
		postRt.setTable("NAT");
		Chain preRt = new Chain();
		preRt.setName("PREROUTING");
		preRt.setTable("NAT");

		host.addFwChain(postRt);
		host.addFwChain(preRt);

		session.saveAll(host.getFirewallChains());
		session.save(host, 7);
	}

	@SuppressWarnings("deprecation")
	private static Host getHost(Session session, String hostname) {
		Host host = session.load(qryNode().where(Host.class)
				.whereParam("hostname", hostname)
				.setLazy(true)
				.asRead()
				.getResult());
		if (host == null) {
			host = new Host();
			host.setHostname(hostname);
			return host;
		}
		session.resolveLazyLoaded(host);
		return host;
	}

	private static NodeQueryBuilder qryNode() {
		return qryBuilder.node();
	}

	private static RelationQueryBuilder qryRelation() {
		return qryBuilder.relation();
	}

	private static Configuration configureDB() {
		Neo4jConfiguration dbCnf = new Neo4jConfiguration("10.88.0.18");
		// register model package
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model");
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.firewall");
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.server");
		// set classloader
		dbCnf.addClassLoader(Launcher.class.getClassLoader());
		// set credentials
		dbCnf.setUser("neo4j");
		dbCnf.setPassword("glowmoss");
		return dbCnf;
	}

}
