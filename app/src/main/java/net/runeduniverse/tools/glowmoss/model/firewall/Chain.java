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
import java.util.LinkedList;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.PostLoad;
import net.runeduniverse.lib.rogm.annotations.PreSave;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.lib.rogm.annotations.Transient;

/*
 * Chains are containers for rules. They exist in two kinds, base chains and regular chains. A base chain is an
 * entry point for packets from the networking stack, a regular chain may be used as jump target and is used
 * for better rule organization.
 */
@NodeEntity(label = "CHAIN")
@Accessors(chain = true)
public class Chain extends ANamedEntity {

	public static final String LABEL_REL_TABLE = "HAS_CHAIN";

	@Relationship(label = LABEL_REL_TABLE, direction = Direction.INCOMING)
	@Getter
	@Setter
	protected Table table;

	@Relationship(label = Rule.REL_LABEL_JUMP, direction = Direction.INCOMING)
	@Getter(onMethod_ = { @Deprecated })
	protected final Set<Rule> _jumpSources = new LinkedHashSet<>();

	public Set<Rule> getJumpSources() {
		return new LinkedHashSet<Rule>(this._jumpSources);
	}

	@Relationship(label = Rule.REL_LABEL_GOTO, direction = Direction.INCOMING)
	@Getter(onMethod_ = { @Deprecated })
	protected final Set<Rule> _gotoSources = new LinkedHashSet<>();

	public Set<Rule> getGotoSources() {
		return new LinkedHashSet<Rule>(this._gotoSources);
	}

	public Chain setName(String name) {
		this.name = name;
		return this;
	}

	@Relationship(label = "NEXT")
	protected Rule _firstRule;

	@Transient
	protected LinkedList<Rule> _rules = new LinkedList<>();

	public LinkedList<Rule> getRules() {
		return new LinkedList<Rule>(this._rules);
	}

	@SuppressWarnings("deprecation")
	public Chain addRule(Rule rule) {
		this._rules.add(rule);
		rule.set_chain(this);
		return this;
	}

	@SuppressWarnings("deprecation")
	public Chain removeRule(Rule rule) {
		this._rules.remove(rule);
		rule.set_chain(null);
		return this;
	}

	@SuppressWarnings("deprecation")
	@PostLoad
	private void postLoad() {
		synchronized (this._rules) {
			this._rules.clear();
			for (Rule r = this._firstRule; r != null; r = r.get_next()) {
				this._rules.add(r);
				r.set_chain(this);
			}
		}
	}

	@PreSave
	@SuppressWarnings("deprecation")
	private void preSave() {
		synchronized (this._rules) {
			Rule last = null;
			for (Rule rule : this._rules) {
				if (last == null) {
					this._firstRule = rule;
				} else {
					last.set_next(rule);
				}
				last = rule;
			}
		}
	}
}
