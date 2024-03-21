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

import lombok.Getter;
import lombok.Setter;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.RelationshipEntity;
import net.runeduniverse.lib.rogm.annotations.StartNode;
import net.runeduniverse.lib.rogm.annotations.TargetNode;
import net.runeduniverse.tools.glowmoss.model.ARelationEntity;

@Getter
@RelationshipEntity(direction = Direction.BIDIRECTIONAL, label = "NW_PEER")
public class Peer extends ARelationEntity {

	@Setter
	private String label;

	@StartNode
	private Interface if0;

	public void setIf0(Interface if0) {
		this.if0 = if0;
		if0.setPeer(this);
	}

	@TargetNode
	private Interface if1;

	public void setIf1(Interface if1) {
		this.if1 = if1;
		if1.setPeer(this);
	}

}
