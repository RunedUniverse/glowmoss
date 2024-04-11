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
package net.runeduniverse.tools.glowmoss.model.firewall.arp;

import lombok.Getter;
import lombok.Setter;
import net.runeduniverse.lib.rogm.annotations.NodeEntity;
import net.runeduniverse.lib.rogm.annotations.Relationship;
import net.runeduniverse.tools.glowmoss.model.firewall.Hook;
import net.runeduniverse.tools.glowmoss.model.firewall.ip.OutputHook;

@NodeEntity(label = InputArpHook.LABEL)
@Getter
@Setter
public class InputArpHook extends ArpHook implements Hook {

	protected static final String LABEL = "INPUT_ARP";

	@Relationship(label = "NEXT")
	protected ArpHandler handler;

}