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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Getter;
import net.runeduniverse.tools.glowmoss.model.firewall.app.LocalProcess;
import net.runeduniverse.tools.glowmoss.model.firewall.arp.ArpHandler;
import net.runeduniverse.tools.glowmoss.model.firewall.arp.InputArpHook;
import net.runeduniverse.tools.glowmoss.model.firewall.arp.OutputArpHook;
import net.runeduniverse.tools.glowmoss.model.firewall.bridge.ForwardBridgeHook;
import net.runeduniverse.tools.glowmoss.model.firewall.bridge.InputBridgeHook;
import net.runeduniverse.tools.glowmoss.model.firewall.bridge.OutputBridgeHook;
import net.runeduniverse.tools.glowmoss.model.firewall.bridge.PostroutingBridgeHook;
import net.runeduniverse.tools.glowmoss.model.firewall.bridge.PreroutingBridgeHook;
import net.runeduniverse.tools.glowmoss.model.firewall.ip.ForwardHook;
import net.runeduniverse.tools.glowmoss.model.firewall.ip.InputHook;
import net.runeduniverse.tools.glowmoss.model.firewall.ip.OutputHook;
import net.runeduniverse.tools.glowmoss.model.firewall.ip.PostroutingHook;
import net.runeduniverse.tools.glowmoss.model.firewall.ip.PreroutingHook;

@Getter
public class FirewallHandler {

	// Application Layer
	protected LocalProcess localProcess;

	// Ip Layer
	protected PreroutingHook ipHookPrerouting;
	protected InputHook ipHookInput;
	protected ForwardHook ipHookForward;
	protected OutputHook ipHookOutput;
	protected PostroutingHook ipHookPostrouting;

	// Bridge Layer
	protected PreroutingBridgeHook bridgeHookPrerouting;
	protected InputBridgeHook bridgeHookInput;
	protected ForwardBridgeHook bridgeHookForward;
	protected OutputBridgeHook bridgeHookOutput;
	protected PostroutingBridgeHook bridgeHookPostrouting;

	// ARP Layer
	protected InputArpHook arpHookInput;
	protected ArpHandler arpHandler;
	protected OutputArpHook arpHookOutput;

	// NO Layer
	protected IngressHook hookIngress;
	protected EgressHook hookEgress;

	public FirewallHandler() {
	}

	void init() {
		localProcess = new LocalProcess();

		// Ip Layer
		ipHookPrerouting = new PreroutingHook();
		ipHookInput = new InputHook();
		ipHookForward = new ForwardHook();
		ipHookOutput = new OutputHook();
		ipHookPostrouting = new PostroutingHook();

		// Bridge Layer
		bridgeHookPrerouting = new PreroutingBridgeHook();
		bridgeHookInput = new InputBridgeHook();
		bridgeHookForward = new ForwardBridgeHook();
		bridgeHookOutput = new OutputBridgeHook();
		bridgeHookPostrouting = new PostroutingBridgeHook();

		// ARP Layer
		arpHookInput = new InputArpHook();
		arpHandler = new ArpHandler();
		arpHookOutput = new OutputArpHook();

		// NO Layer
		hookIngress = new IngressHook();
		hookEgress = new EgressHook();
	}

	protected void init(Collection<Object> entities) {
		if (entities == null) {
			return;
		}

		for (Object obj : entities) {
			set(obj, LocalProcess.class, h -> this.localProcess = h);
			set(obj, PreroutingHook.class, h -> this.ipHookPrerouting = h);
			set(obj, InputHook.class, h -> this.ipHookInput = h);
			set(obj, ForwardHook.class, h -> this.ipHookForward = h);
			set(obj, OutputHook.class, h -> this.ipHookOutput = h);
			set(obj, PostroutingHook.class, h -> this.ipHookPostrouting = h);
			set(obj, PreroutingBridgeHook.class, h -> this.bridgeHookPrerouting = h);
			set(obj, InputBridgeHook.class, h -> this.bridgeHookInput = h);
			set(obj, ForwardBridgeHook.class, h -> this.bridgeHookForward = h);
			set(obj, OutputBridgeHook.class, h -> this.bridgeHookOutput = h);
			set(obj, PostroutingBridgeHook.class, h -> this.bridgeHookPostrouting = h);
			set(obj, InputArpHook.class, h -> this.arpHookInput = h);
			set(obj, ArpHandler.class, h -> this.arpHandler = h);
			set(obj, OutputArpHook.class, h -> this.arpHookOutput = h);
			set(obj, IngressHook.class, h -> this.hookIngress = h);
			set(obj, EgressHook.class, h -> this.hookEgress = h);
		}
	}

	protected void relink() {
		///////////////////////////////////////////////////////////////////////
		// FLOW
		///////////////////////////////////////////////////////////////////////

		// bridge
		hookIngress.setNextIfBridgePort(bridgeHookPrerouting);

		bridgeHookPrerouting.setNextIfIp(bridgeHookInput);
		bridgeHookPrerouting.setNextIfOther(bridgeHookForward);

		bridgeHookInput.setNext(ipHookPrerouting);

		bridgeHookForward.setNext(bridgeHookPostrouting);

		bridgeHookOutput.setNext(bridgeHookPostrouting);

		bridgeHookPostrouting.setNext(hookEgress);

		// ip
		hookIngress.setNextIfIpProtocol(ipHookPrerouting);

		ipHookPrerouting.setNextIfLocal(ipHookInput);
		ipHookPrerouting.setNextIfOther(ipHookForward);

		ipHookInput.setNext(localProcess);

		localProcess.setNext(ipHookOutput);

		ipHookForward.setNext(ipHookPostrouting);

		ipHookOutput.setNext(ipHookPostrouting);

		ipHookPostrouting.setNextIfBridgeDevice(bridgeHookOutput);
		ipHookPostrouting.setNextIfOther(hookEgress);

		// arp
		hookIngress.setNextIfArpProtocol(arpHookInput);

		arpHookInput.setHandler(arpHandler);

		arpHandler.setNext(arpHookOutput);

		arpHookOutput.setEgress(hookEgress);

	}

	protected static <T> T get(T obj, Supplier<T> supplier) {
		if (obj == null)
			return supplier.get();
		return obj;
	}

	protected static <T> void set(Object obj, Class<T> type, Consumer<T> consumer) {
		if (type.isInstance(obj))
			consumer.accept((T) obj);
	}

	public static FirewallHandler create() {
		final FirewallHandler handler = new FirewallHandler();

		handler.init();
		handler.relink();

		return handler;
	}

	public Collection<Object> nodes() {
		final Set<Object> nodes = new LinkedHashSet<>();

		nodes.add(localProcess);
		nodes.add(ipHookPrerouting);
		nodes.add(ipHookInput);
		nodes.add(ipHookForward);
		nodes.add(ipHookOutput);
		nodes.add(ipHookPostrouting);
		nodes.add(bridgeHookPrerouting);
		nodes.add(bridgeHookInput);
		nodes.add(bridgeHookForward);
		nodes.add(bridgeHookOutput);
		nodes.add(bridgeHookPostrouting);
		nodes.add(arpHookInput);
		nodes.add(arpHandler);
		nodes.add(arpHookOutput);
		nodes.add(hookIngress);
		nodes.add(hookEgress);

		return nodes;
	}

}
