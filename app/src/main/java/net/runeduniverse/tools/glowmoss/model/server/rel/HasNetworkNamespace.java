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
package net.runeduniverse.tools.glowmoss.model.server.rel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.RelationshipEntity;
import net.runeduniverse.lib.rogm.annotations.StartNode;
import net.runeduniverse.lib.rogm.annotations.TargetNode;
import net.runeduniverse.tools.glowmoss.model.ARelationEntity;
import net.runeduniverse.tools.glowmoss.model.network.Namespace;
import net.runeduniverse.tools.glowmoss.model.server.Host;

@RelationshipEntity(direction = Direction.OUTGOING, label = "has_NW_NAMESPACE")
@Getter
@Setter
@NoArgsConstructor
public class HasNetworkNamespace extends ARelationEntity {

	private Integer key;

	public HasNetworkNamespace(Integer key, Host host) {
		this.key = key;
		this.host = host;
	}

	@StartNode
	private Host host;

	@TargetNode
	private Namespace namespace;

	public static Integer getKey(final HasNetworkNamespace container) {
		if (container == null)
			return null;
		return container.getKey();
	}

	public static Host getHost(final HasNetworkNamespace container) {
		if (container == null)
			return null;
		return container.getHost();
	}

	public static Namespace getNamespace(final HasNetworkNamespace container) {
		if (container == null)
			return null;
		return container.getNamespace();
	}
}
