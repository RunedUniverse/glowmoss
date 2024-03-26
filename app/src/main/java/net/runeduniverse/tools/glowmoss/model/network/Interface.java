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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.tools.glowmoss.model.AEntity;

@Getter
@NodeEntity(label = "NW_INTERFACE")
public class Interface extends AEntity {

	@Setter
	private String label;

	@Setter
	private String type = "veth";

	@Setter
	private Boolean virtual = true;

	@Relationship(label = "LINKED", direction = Direction.BIDIRECTIONAL)
	private Set<Interface> links = new LinkedHashSet<>();

	public Set<Interface> getLinks() {
		return Collections.unmodifiableSet(this.links);
	}

	public void link(Interface if0) {
		synchronized (this.links) {
			synchronized (if0.links) {
				this.links.add(if0);
				if0.links.add(this);
			}
		}
	}

	public void unlink(Interface if0) {
		synchronized (this.links) {
			synchronized (if0.links) {
				this.links.remove(if0);
				if0.links.remove(this);
			}
		}
	}

	@Setter
	private Boolean up = false;

	@Relationship(label = "has_NW_INTERFACE", direction = Direction.INCOMING)
	@Setter(value = AccessLevel.PROTECTED)
	private Namespace namespace;

	@Relationship(label = "CONNECTED_TO", direction = Direction.OUTGOING)
	@Setter(value = AccessLevel.PROTECTED)
	private Bridge bridge;

}
