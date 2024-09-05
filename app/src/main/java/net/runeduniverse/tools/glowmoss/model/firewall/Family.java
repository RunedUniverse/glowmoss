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

public enum Family {

	NETDEV("netdev"), INET("inet"), IP("ip"), IP6("ip6"), BRIDGE("bridge"), ARP("arp");

	private final String text;

	private Family(final String text) {
		this.text = text;
	}

	public String text() {
		return this.text;
	}

	public static Family find(String text) {
		for (Family family : values()) {
			if (family.text()
					.equals(text))
				return family;
		}
		return null;
	}
}
