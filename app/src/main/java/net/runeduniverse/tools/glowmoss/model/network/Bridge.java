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
package net.runeduniverse.tools.glowmoss.model.network;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.tools.glowmoss.model.AEntity;

@NodeEntity(label = "NW_BRIDGE")
@Getter
public class Bridge extends AEntity {

	@Setter
	private String label;

	@Setter
	@Relationship
	private Namespace namespace;

	@Relationship(label = "CONNECTED_TO", direction = Direction.BIDIRECTIONAL)
	private Set<Interface> interfaces = new LinkedHashSet<>();

	public void addInterface(Interface if0) {
		this.interfaces.add(if0);
		if0.setBridge(this);
	}

}
