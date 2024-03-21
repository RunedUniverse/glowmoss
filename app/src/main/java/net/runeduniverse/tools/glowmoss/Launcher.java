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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.runeduniverse.lib.rogm.Configuration;
import net.runeduniverse.lib.rogm.Session;
import net.runeduniverse.lib.rogm.modules.neo4j.Neo4jConfiguration;
import net.runeduniverse.lib.rogm.querying.QueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.NodeQueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.RelationQueryBuilder;
import net.runeduniverse.tools.glowmoss.model.firewall.Chain;
import net.runeduniverse.tools.glowmoss.model.network.Bridge;
import net.runeduniverse.tools.glowmoss.model.network.Interface;
import net.runeduniverse.tools.glowmoss.model.network.Namespace;
import net.runeduniverse.tools.glowmoss.model.network.NetworkFactory;
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
		List<Object> obs = new LinkedList<>();
		Host host = getHost(session, "rocky-vm");
		obs.add(host);

		Chain postRt = new Chain();
		postRt.setName("POSTROUTING");
		postRt.setTable("NAT");
		obs.add(postRt);
		Chain preRt = new Chain();
		preRt.setName("PREROUTING");
		preRt.setTable("NAT");
		obs.add(preRt);

		host.addFwChain(postRt);
		host.addFwChain(preRt);

		session.saveAll(host.getFirewallChains());
		session.save(host, 7);

		///////////////////////////////////////////////////////////////////////

		Namespace ns0 = host.getNamespaces()
				.get(0);
		obs.add(ns0);
		Namespace ns1 = new Namespace();
		obs.add(ns1);
		host.putNamespace(1, ns1);
		ns1.setLabel("pod-1");
		ns1.setUuid(UUID.randomUUID());
		ns1.setNet_ipv4_IpForward(false);

		Interface wan = NetworkFactory.createInterface("wan");
		Interface lan = NetworkFactory.createInterface("lan");

		obs.add(wan);
		obs.add(lan);

		Interface veth0 = NetworkFactory.createVirtualInterface("veth0");
		Interface veth1 = NetworkFactory.createVirtualInterface("veth1");
		obs.add(veth0);
		obs.add(veth1);
		obs.add(NetworkFactory.peer(veth0, veth1));

		Bridge bridge0 = NetworkFactory.createBridge("vbridge");
		bridge0.addInterface(wan);
		bridge0.addInterface(lan);
		bridge0.setNamespace(ns0);
		obs.add(bridge0);

		Bridge podman0 = NetworkFactory.createBridge("podman0");
		podman0.addInterface(veth0);
		podman0.setNamespace(ns0);
		obs.add(podman0);

		Bridge pod1 = NetworkFactory.createBridge("pod1");
		pod1.addInterface(veth1);
		pod1.setNamespace(ns1);
		obs.add(pod1);

		session.save(host, 20);
		session.saveAll(obs, 20);
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
			Namespace ns = new Namespace();
			ns.setLabel("default");
			ns.setNet_ipv4_IpForward(true);
			ns.setUuid(UUID.randomUUID());
			host.putNamespace(0, ns);
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
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.network");
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.server");
		dbCnf.addPackage("net.runeduniverse.tools.glowmoss.model.server.rel");
		// set classloader
		dbCnf.addClassLoader(Launcher.class.getClassLoader());
		// set credentials
		dbCnf.setUser("neo4j");
		dbCnf.setPassword("glowmoss");
		return dbCnf;
	}

}
