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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.tools.glowmoss.model.AEntity;

@Getter
@NodeEntity(label = "RULE")
@Accessors(chain = true)
public class Rule extends AEntity {

	private static final String REL_LABEL_JUMP = "JUMP";
	private static final String REL_LABEL_GOTO = "GOTO";

	@Setter(onMethod_ = { @Deprecated })
	@Relationship(label = "NEXT", direction = Direction.OUTGOING)
	private Rule next;

	@Setter
	@Relationship(label = REL_LABEL_JUMP, direction = Direction.BIDIRECTIONAL)
	private Chain jumpTo = null;

	@Setter
	@Relationship(label = REL_LABEL_GOTO, direction = Direction.OUTGOING)
	private Chain goTo = null;

	@Setter
	private String content;

}
