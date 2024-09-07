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
import net.runeduniverse.lib.rogm.annotations.Transient;
import net.runeduniverse.tools.glowmoss.model.AEntity;

@NodeEntity(label = "RULE")
@Accessors(chain = true)
public class Rule extends AEntity {

	public static final String REL_LABEL_JUMP = "JUMP";
	public static final String REL_LABEL_GOTO = "GOTO";

	@Setter(onMethod_ = { @Deprecated })
	@Transient
	protected Chain _chain = null;

	public Chain getChain() {
		return this._chain;
	}

	@Getter
	@Setter(onMethod_ = { @Deprecated })
	@Relationship(label = "NEXT", direction = Direction.OUTGOING)
	protected Rule _next;

	@Relationship(label = REL_LABEL_JUMP, direction = Direction.OUTGOING)
	protected Chain _jumpTo = null;

	public Chain getJumpTo() {
		return this._jumpTo;
	}

	public Rule setJumpTo(Chain jumpTarget) {
		if (jumpTarget == null) {
			if (this._jumpTo != null)
				this._jumpTo.get_jumpSources()
						.remove(this);
		} else {
			jumpTarget.get_jumpSources()
					.add(this);
		}
		this._jumpTo = jumpTarget;
		return this;
	}

	@Relationship(label = REL_LABEL_GOTO, direction = Direction.OUTGOING)
	protected Chain _goTo = null;

	public Chain getGoTo() {
		return this._goTo;
	}

	public Rule setGoTo(Chain gotoTarget) {
		if (gotoTarget == null) {
			if (this._goTo != null)
				this._goTo.get_gotoSources()
						.remove(this);
		} else {
			gotoTarget.get_gotoSources()
					.add(this);
		}
		this._goTo = gotoTarget;
		return this;
	}

	public boolean hasTargetRef() {
		return this._jumpTo != null || this._goTo != null;
	}

	@Getter
	@Setter
	protected String content;

}
