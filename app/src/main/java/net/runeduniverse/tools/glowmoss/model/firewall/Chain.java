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
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.PostLoad;
import net.runeduniverse.lib.rogm.annotations.PreSave;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.lib.rogm.annotations.Transient;
import net.runeduniverse.tools.glowmoss.model.AEntity;

/*
 * Chains are containers for rules. They exist in two kinds, base chains and regular chains. A base chain is an
 * entry point for packets from the networking stack, a regular chain may be used as jump target and is used
 * for better rule organization.
 */
@NodeEntity(label = "CHAIN")
@Accessors(chain = true)
public class Chain extends AEntity {

	public static final String LABEL_REL_TABLE = "HAS_CHAIN";

	@Relationship(label = LABEL_REL_TABLE, direction = Direction.INCOMING)
	@Getter
	@Setter
	private Table table;

	@Getter
	@Setter
	private String name;

	// default = 'filter'
	@Getter
	@Setter
	private ChainType type = ChainType.FILTER;

	@Getter
	@Setter
	private String policy = null;

	@Relationship(label = "NEXT")
	private Rule firstRule;

	@Getter
	@Transient
	private List<Rule> rules = new LinkedList<>();

	public Chain addRule(Rule rule) {
		this.rules.add(rule);
		return this;
	}

	public Chain removeRule(Rule rule) {
		this.rules.remove(rule);
		return this;
	}

	@PostLoad
	private void postLoad() {
		synchronized (this.rules) {
			this.rules.clear();
			for (Rule r = this.firstRule; r != null; r = r.getNext()) {
				this.rules.add(r);
			}
		}
	}

	@PreSave
	@SuppressWarnings("deprecation")
	private void preSave() {
		synchronized (this.rules) {
			Rule last = null;
			for (Rule rule : this.rules) {
				if (last == null) {
					this.firstRule = rule;
				} else {
					last.setNext(rule);
				}
				last = rule;
			}
		}
	}

}
