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
package net.runeduniverse.tools.glowmoss.model.firewall;

import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.Relationship;

/*
 * Tables are containers for chains, sets and stateful objects. They are identified by their address family and
 * their name. The address family must be one of ip, ip6, inet, arp, bridge, netdev. The inet address family is
 * a dummy family which is used to create hybrid IPv4/IPv6 tables. The meta expression nfproto keyword can be
 * used to test which family (ipv4 or ipv6) context the packet is being processed in. When no address family is
 * specified, ip is used by default. The only difference between add and create is that the former will not
 * return an error if the specified table already exists while create will return an error.
 */
@Getter
@NodeEntity(label = "TABLE")
@Accessors(chain = true)
public class Table extends ANamedEntity {

	// IP is default
	public static final Family DEFAULT_FAMILY = Family.IP;

	@Setter
	protected Family family = DEFAULT_FAMILY;

	public Table setName(String name) {
		this.name = name;
		return this;
	}

	// flags
	/*
	 * table is not evaluated any more (base chains are unregistered).
	 */
	@Setter
	protected Boolean dormant = false;

	@Relationship(label = Chain.LABEL_REL_TABLE)
	protected List<Chain> chains = new LinkedList<>();

	protected List<String> sets = new LinkedList<>();

	protected List<String> statefulObjects = new LinkedList<>();

	public BaseChain createBaseChain(final String name, final ChainType type, final Hook hook, final Integer priority) {
		final BaseChain chain = new BaseChain();
		chain.setName(name);
		chain.setType(type);
		chain.setHook(hook);
		chain.setEffPriority(priority);
		addChain(chain);
		return chain;
	}

	public Chain createChain(final String name, final ChainType type) {
		final Chain chain = new Chain();
		chain.setName(name);
		addChain(chain);
		return chain;
	}

	public Table addChain(final Chain chain) {
		if (chain == null)
			return this;
		synchronized (this.chains) {
			chain.setTable(this);
			this.chains.add(chain);
		}
		return this;
	}

	public Table removeChain(final Chain chain) {
		synchronized (this.chains) {
			chain.setTable(null);
			this.chains.remove(chain);
		}
		return this;
	}
}
