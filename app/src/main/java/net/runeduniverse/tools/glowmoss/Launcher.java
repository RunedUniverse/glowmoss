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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import net.runeduniverse.lib.rogm.Configuration;
import net.runeduniverse.lib.rogm.Session;
import net.runeduniverse.lib.rogm.lang.Language.IMapper;
import net.runeduniverse.lib.rogm.pipeline.DatabasePipelineFactory;
import net.runeduniverse.lib.rogm.pipeline.Pipeline;
import net.runeduniverse.lib.rogm.pipeline.chain.Chains;
import net.runeduniverse.lib.rogm.querying.QueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.NodeQueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.RelationQueryBuilder;
import net.runeduniverse.lib.utils.chain.ChainManager;
import net.runeduniverse.tools.glowmoss.model.firewall.Firewall;
import net.runeduniverse.tools.glowmoss.model.network.Bridge;
import net.runeduniverse.tools.glowmoss.model.network.Interface;
import net.runeduniverse.tools.glowmoss.model.network.Namespace;
import net.runeduniverse.tools.glowmoss.model.network.NetworkFactory;
import net.runeduniverse.tools.glowmoss.model.server.Host;
import net.runeduniverse.tools.glowmoss.parser.FirewallParser;

public class Launcher {

	private static ConsoleLogger logger = new ConsoleLogger(Logger.getLogger(Launcher.class.getName()));

	private static Options options = new Options();
	private static Configuration dbCnf;
	private static QueryBuilder qryBuilder;

	public static void main(String[] args) {
		RogmPatches.patch();

		if (!options.init(args)) {
			System.err.println("Invalid arguments detected, stopping!");
			return;
		}

		dbCnf = options.dbConfig();

		if (options.debug()) {
			dbCnf.setLogger(logger);
		}

		final Pipeline pipe = new Pipeline(new DatabasePipelineFactory(dbCnf) {

			@Override
			protected void setupChainManager(ChainManager chainManager) throws Exception {
				chainManager.addChainLayers(Launcher.class);
				RogmPatches.patch(chainManager);
				super.setupChainManager(chainManager);
			}

		});

		try (Session dbSession = pipe.buildSession()) {
			qryBuilder = dbSession.getQueryBuilder();

			// initHost(dbSession);
			createFW(dbSession);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@net.runeduniverse.lib.utils.chain.Chain(label = Chains.SAVE_CHAIN.ONE.LABEL, layers = { 199 })
	public static void debug(IMapper mapper) {
		if (!options.log())
			return;
		System.err.println();
		System.err.println();
		System.err.println(mapper.qry());
	}

	private static void createFW(Session session) {
		Firewall firewall = Firewall.create();

		FirewallParser parser = new FirewallParser(firewall);

		Path ruleset = options.nftRuleset();
		if (ruleset == null)
			return;

		try {
			parser.parse(ruleset);
			// parser.parse(Paths.get("/data/userdata/code/java/RunedUniverse/glowmoss/src/main/resources",
			// "ruleset.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		firewall.save(session);
		// session.save(table, 20);
		session.saveAll(parser.getResults(), Integer.MAX_VALUE);
	}

	private static void initHost(Session session) {
		Host host = getHost(session, "rocky-vm");

		session.save(host, 7);

		///////////////////////////////////////////////////////////////////////

		List<Object> obs = new LinkedList<>();
		Namespace ns0 = host.getNamespace(0);
		obs.add(ns0);
		Namespace ns1 = new Namespace();
		obs.add(ns1);
		host.putNamespace(1, ns1);
		ns1.setLabel("pod-1");
		ns1.setUuid(UUID.randomUUID());
		ns1.setNet_ipv4_IpForward(false);

		Interface wan = NetworkFactory.createInterface("wan");
		Interface lan = NetworkFactory.createInterface("lan");

		Interface veth0 = NetworkFactory.createVirtualInterface("veth0");
		Interface veth1 = NetworkFactory.createVirtualInterface("veth1");
		veth0.link(veth1);

		Bridge bridge0 = NetworkFactory.createBridge("vbridge");
		bridge0.addInterface(wan);
		bridge0.addInterface(lan);
		ns0.addBridge(bridge0);

		Bridge podman0 = NetworkFactory.createBridge("podman0");
		podman0.addInterface(veth0);
		ns0.addBridge(podman0);

		Bridge pod1 = NetworkFactory.createBridge("pod1");
		pod1.addInterface(veth1);
		ns1.addBridge(pod1);

		log(ns0);
		log(ns1);

		session.save(ns0, 20);
		session.save(ns1, 20);
	}

	public static void log(Namespace ns) {
		System.out.println("\n");
		System.out.println("Namespace:");
		System.out.println("  UUID: " + ns.getUuid());
		System.out.println("  Bridges:");
		for (Bridge b : ns.getBridges()) {
			System.out.println("    : " + b.getLabel());
		}
		System.out.println("  Interfaces:");
		for (Interface i : ns.getInterfaces()) {
			System.out.println("    : " + i.getLabel());
		}
		System.out.println("  " + ns);
		System.out.println("\n");
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

}
