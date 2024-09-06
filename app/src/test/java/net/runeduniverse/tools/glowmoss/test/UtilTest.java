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
package net.runeduniverse.tools.glowmoss.test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import net.runeduniverse.tools.glowmoss.model.firewall.Firewall;
import net.runeduniverse.tools.glowmoss.model.firewall.bridge.OutputBridgeHook;
import net.runeduniverse.tools.glowmoss.model.firewall.ip.PreroutingHook;

import static net.runeduniverse.tools.glowmoss.TermUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class UtilTest {

	@Test
	@Tag("system")
	public void testArithmeticCalc() {
		final String term = "mangle - 5";
		final LinkedList<String> splitTerm = new LinkedList<>();
		if (!trySplitTerm(term, splitTerm))
			System.err.println("splitting failed!");

		final Map<String, Integer> varMap = new LinkedHashMap<>();
		varMap.put("mangle", -150);
		varMap.put("filter", 0);

		Integer result = calcSplitTermAsInteger(splitTerm, varMap);
		System.out.println("Result: " + result);
		assertEquals(-155, result);
	}

	@Test
	@Tag("system")
	public void testArithmeticCalc2() {
		final String term = "filter - 5";
		final LinkedList<String> splitTerm = new LinkedList<>();
		if (!trySplitTerm(term, splitTerm))
			System.err.println("splitting failed!");

		final Map<String, Integer> varMap = Firewall.priorityMap(new PreroutingHook());

		Integer result = calcSplitTermAsInteger(splitTerm, varMap);
		System.out.println("Result: " + result);
		assertEquals(-5, result);
	}

	@Test
	@Tag("system")
	public void testArithmeticCalc3() {
		final String term = "filter - 5";
		final LinkedList<String> splitTerm = new LinkedList<>();
		if (!trySplitTerm(term, splitTerm))
			System.err.println("splitting failed!");

		final Map<String, Integer> varMap = Firewall.priorityMap(new OutputBridgeHook());

		Integer result = calcSplitTermAsInteger(splitTerm, varMap);
		System.out.println("Result: " + result);
		assertEquals(-205, result);
	}
}
