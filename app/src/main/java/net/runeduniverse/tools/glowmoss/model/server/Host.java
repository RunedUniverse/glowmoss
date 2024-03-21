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
package net.runeduniverse.tools.glowmoss.model.server;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.PostLoad;
import net.runeduniverse.lib.rogm.annotations.PreSave;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.lib.rogm.annotations.Transient;
import net.runeduniverse.tools.glowmoss.model.AEntity;
import net.runeduniverse.tools.glowmoss.model.firewall.Chain;
import net.runeduniverse.tools.glowmoss.model.network.Namespace;
import net.runeduniverse.tools.glowmoss.model.server.rel.HasNetworkNamespace;

public class Host extends AEntity {

	@Setter
	@Getter
	private String hostname;

	@Relationship(direction = Direction.OUTGOING)
	private List<HasNetworkNamespace> namespaceRelations = new LinkedList<>();
	@Transient
	private Map<Integer, HasNetworkNamespace> namespaceRelationsRef = new LinkedHashMap<>();

	public Map<Integer, Namespace> getNamespaces() {
		synchronized (this.namespaceRelations) {
			return this.namespaceRelationsRef.entrySet()
					.stream()
					.collect(Collectors.toMap(e -> e.getKey(), e -> HasNetworkNamespace.getNamespace(e.getValue())));
		}
	}

	public Namespace getNamespace(Integer key) {
		synchronized (this.namespaceRelations) {
			return HasNetworkNamespace.getNamespace(this.namespaceRelationsRef.get(key));
		}
	}

	public Namespace removeNamespace(Integer key) {
		HasNetworkNamespace container;
		synchronized (this.namespaceRelations) {
			container = this.namespaceRelationsRef.remove(key);
		}
		Namespace ns = null;
		if (container != null) {
			ns = container.getNamespace();
			ns.setHost(null);
			container.setHost(null);
			container.setNamespace(null);
		}
		return ns;
	}

	public Namespace putNamespace(Integer key, Namespace namespace) {
		System.out.println("Host.putNamespace()");
		System.out.println("Namespace:" + namespace.toString());
		HasNetworkNamespace container;
		synchronized (this.namespaceRelations) {
			container = this.namespaceRelationsRef.get(key);
			if (container == null) {
				this.namespaceRelationsRef.put(key, container = new HasNetworkNamespace(key, this));
			}
			Namespace old = container.getNamespace();
			if (old != null) {
				old.setHost(null);
			}
			container.setNamespace(namespace);
			namespace.setHost(container);
			return old;
		}
	}

	@PostLoad
	private void postLoad() {
		// has_NW_NAMESPACE
		synchronized (this.namespaceRelations) {
			this.namespaceRelationsRef.clear();
			this.namespaceRelationsRef.putAll(this.namespaceRelations.stream()
					.collect(Collectors.toMap(e -> e.getKey(), e -> e)));
		}
	}

	@PreSave
	private void preSave() {
		// has_NW_NAMESPACE
		synchronized (this.namespaceRelations) {
			this.namespaceRelations.clear();
			this.namespaceRelations.addAll(this.namespaceRelationsRef.values());
		}
	}

	@Relationship(direction = Direction.OUTGOING)
	@Getter
	private Set<Host> virtualHosts = new LinkedHashSet<>();

	@Setter
	@Getter
	private Boolean isContainer;

	@Relationship(direction = Direction.OUTGOING, label = "has_FW_CHAIN")
	@Getter
	private Set<Chain> firewallChains = new LinkedHashSet<>();

	public void addFwChain(Chain chain) {
		this.firewallChains.add(chain);
		chain.setHost(this);
	}

}
