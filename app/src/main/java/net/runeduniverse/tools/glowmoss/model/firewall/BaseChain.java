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
import net.runeduniverse.lib.rogm.annotations.Property;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.lib.rogm.annotations.Transient;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;
import static net.runeduniverse.tools.glowmoss.Util.*;

import java.util.LinkedList;
import java.util.Map;

@NodeEntity(label = "BASE_CHAIN")
@Accessors(chain = true)
public class BaseChain extends Chain {

	public static final String REL_LABEL_HOOK = "CALLS";

	// default = 'filter'
	@Getter
	@Setter
	protected ChainType type = ChainType.FILTER;

	@Getter
	@Setter
	@Transient
	@Relationship(label = REL_LABEL_HOOK, direction = Direction.INCOMING)
	protected Hook hook;

	@Getter
	@Setter
	// only on INGRESS & EGRESS
	protected String device = null;

	@Transient
	protected final Object _priorityLock = new Object();
	@Property(tag = "priority")
	protected String _priority = "";
	@Property(tag = "effPriority")
	protected Integer _effPriority = 0;

	public String getPriority() {
		synchronized (this._priorityLock) {
			return this._priority;
		}
	}

	public Integer getEffPriority() {
		synchronized (this._priorityLock) {
			return this._effPriority;
		}
	}

	public BaseChain setPriority(String priority) {
		synchronized (this._priorityLock) {
			if (isBlank(priority))
				throw new NumberFormatException("null");

			priority = priority.toLowerCase();

			final LinkedList<String> splitTerm = new LinkedList<>();
			if (!trySplitTerm(priority, splitTerm))
				throw new NumberFormatException("invalid term");

			final Map<String, Integer> varMap;
			if (this.table == null)
				varMap = Firewall.priorityMap(this.hook);
			else
				varMap = Firewall.priorityMap(this.table.getFamily(), this.hook);
			final Integer p = calcSplitTermAsInteger(splitTerm, varMap);

			this._priority = priority;
			this._effPriority = p;
		}
		return this;
	}

	public BaseChain setEffPriority(Integer effPriority) {
		synchronized (this._priorityLock) {
			if (effPriority == null)
				throw new NumberFormatException("null");
			this._effPriority = effPriority;
			this._priority = this._effPriority.toString();
		}
		return this;
	}

	protected void resetPriority() {
		// when Bridge Family => -200 else => 0
		setPriority("filter");
	}

	@Setter
	protected String policy = null;

}
