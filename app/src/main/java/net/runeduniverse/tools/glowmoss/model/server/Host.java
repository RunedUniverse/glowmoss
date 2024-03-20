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

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.tools.glowmoss.model.AEntity;
import net.runeduniverse.tools.glowmoss.model.firewall.Chain;

@Getter
public class Host extends AEntity {

	@Setter
	private String hostname;

	@Relationship(direction = Direction.OUTGOING)
	private Set<Host> virtualHosts = new LinkedHashSet<>();

	@Setter
	private Boolean isContainer;

	@Relationship(direction = Direction.OUTGOING, label = "has_FW_CHAIN")
	private Set<Chain> firewallChains = new LinkedHashSet<>();

	public void addFwChain(Chain chain) {
		this.firewallChains.add(chain);
		chain.setHost(this);
	}

}
