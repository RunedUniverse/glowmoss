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

public class NetworkFactory {

	public static Interface createInterface() {
		return createInterface(null);
	}

	public static Interface createInterface(String label) {
		Interface i = new Interface();
		i.setLabel(label);
		i.setType("eth");
		i.setUp(false);
		i.setVirtual(false);
		return i;
	}

	public static Interface createVirtualInterface() {
		return createVirtualInterface(null);
	}

	public static Interface createVirtualInterface(String label) {
		Interface i = new Interface();
		i.setLabel(label);
		i.setType("veth");
		i.setUp(false);
		i.setVirtual(true);
		return i;
	}

	public static Bridge createBridge() {
		return createBridge(null);
	}

	public static Bridge createBridge(String label) {
		Bridge b = new Bridge();
		b.setLabel(label);
		return b;
	}

	public static Peer peer(Interface if0, Interface if1) {
		return peer(null, if0, if1);
	}

	public static Peer peer(String label, Interface if0, Interface if1) {
		Peer p = new Peer();
		p.setLabel(label);
		p.setIf0(if0);
		p.setIf1(if1);
		return p;
	}

}
