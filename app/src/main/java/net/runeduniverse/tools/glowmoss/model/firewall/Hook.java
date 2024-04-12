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

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.tools.glowmoss.model.AEntity;

@Getter
@NoArgsConstructor
@NodeEntity(label = "HOOK")
public class Hook extends AEntity {

	private final Set<Family> families = new LinkedHashSet<>();

	private Layer layer = Layer.NONE;

	@Relationship(label = BaseChain.REL_LABEL_HOOK, direction = Direction.OUTGOING)
	private Set<BaseChain> chains = new LinkedHashSet<>();

	protected Hook(Layer layer, Family... families) {
		this.layer = layer;
		if (families == null)
			return;
		for (Family family : families) {
			this.families.add(family);
		}
	}

}
