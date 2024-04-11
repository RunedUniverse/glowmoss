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
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.Relationship;

@NodeEntity(label = "BASE_CHAIN")
@Getter
public class BaseChain extends Chain {
	
	public static final String REL_LABEL_HOOK = "CALLS";

	// default = 'filter'
	@Setter
	private ChainType type = ChainType.FILTER;

	@Setter
	@Relationship(label = REL_LABEL_HOOK, direction = Direction.INCOMING)
	private Hook hook;

	@Setter
	// only on INGRESS & EGRESS
	private String device = null;

	// Table 6. Standard priority names, family and hook compatibility matrix
    // ┌─────────┬───────┬─────────────────────┬─────────────┐
    // │Name     │ Value │ Families            │ Hooks       │
    // ├─────────┼───────┼─────────────────────┼─────────────┤
    // │         │       │                     │             │
    // │raw      │ -300  │ ip, ip6, inet       │ all         │
    // ├─────────┼───────┼─────────────────────┼─────────────┤
    // │         │       │                     │             │
    // │mangle   │ -150  │ ip, ip6, inet       │ all         │
    // ├─────────┼───────┼─────────────────────┼─────────────┤
    // │         │       │                     │             │
    // │dstnat   │ -100  │ ip, ip6, inet       │ prerouting  │
    // ├─────────┼───────┼─────────────────────┼─────────────┤
    // │         │       │                     │             │
    // │filter   │ 0     │ ip, ip6, inet, arp, │ all         │
    // │         │       │ netdev              │             │
    // ├─────────┼───────┼─────────────────────┼─────────────┤
    // │         │       │                     │             │
    // │security │ 50    │ ip, ip6, inet       │ all         │
    // ├─────────┼───────┼─────────────────────┼─────────────┤
    // │         │       │                     │             │
    // │srcnat   │ 100   │ ip, ip6, inet       │ postrouting │
    // └─────────┴───────┴─────────────────────┴─────────────┘
    // 
    // Table 7. Standard priority names and hook compatibility for the bridge family
    // ┌───────┬───────┬─────────────┐
    // │       │       │             │
    // │Name   │ Value │ Hooks       │
    // ├───────┼───────┼─────────────┤
    // │       │       │             │
    // │dstnat │ -300  │ prerouting  │
    // ├───────┼───────┼─────────────┤
    // │       │       │             │
    // │filter │ -200  │ all         │
    // ├───────┼───────┼─────────────┤
    // │       │       │             │
    // │out    │ 100   │ output      │
    // ├───────┼───────┼─────────────┤
    // │       │       │             │
    // │srcnat │ 300   │ postrouting │
    // └───────┴───────┴─────────────┘
	
	@Setter
	private String priority = "filter";
	@Setter
	private Integer effPriority = 0;

}
