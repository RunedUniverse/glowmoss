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
package net.runeduniverse.tools.glowmoss;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.List;
import java.util.Map;

public class Util {

	public static boolean trySplitTerm(final String term, final List<String> result) {
		String val = new String();

		boolean wasDigit = false;
		boolean wasText = false;
		boolean wasOperator = false;
		boolean wasMod = false;

		for (char c : term.toCharArray()) {
			// split by whitespace
			if (Character.isWhitespace(c))
				continue;
			// (+/-)
			if ('+' == c || '-' == c) {
				if (wasMod) {
					// mod becomes operator
					if (wasOperator) {
						// error: 2 operators! + + -
						return false;
					}
					wasOperator = true;
				}
				if (wasDigit || wasText || wasOperator) {
					result.add(val);
					val = new String();
					wasDigit = wasText = false;
				}
				val = val + c;
				wasMod = true;
				continue;
			}
			// (0-9)
			if (Character.isDigit(c)) {
				if (wasText) {
					// error: a0
					return false;
				}
				val = val + c;
				wasDigit = true;
				wasOperator = wasMod = false;
				continue;
			}
			// (a-z)
			if (Character.isAlphabetic(c)) {
				if (wasDigit) {
					// error: 0a
					return false;
				}
				if (wasMod) {
					if (wasOperator) {
						// error: - -X => X can be (-100)
						return false;
					}
					// mod becomes operator
					result.add(val);
					val = new String();
				}
				val = val + c;
				wasText = true;
				wasOperator = wasMod = false;
			}
		}
		result.add(val);
		return true;
	}

	/**
	 * Calculate result of a basic arithmetic expression.
	 *
	 * @param splitTerm
	 * @param varMap
	 * @return result of the term
	 * @throws NumberFormatException
	 */
	public static Integer calcSplitTermAsInteger(final List<String> splitTerm, final Map<String, Integer> varMap) {
		if (splitTerm == null || splitTerm.isEmpty())
			throw new NumberFormatException("invalid term");

		boolean add = true;
		Integer num = 0;
		for (String s : splitTerm) {
			if (isBlank(s))
				continue;
			if ("+".equals(s)) {
				add = true;
				continue;
			}
			if ("-".equals(s)) {
				add = false;
				continue;
			}
			Integer val = varMap.get(s);
			if (val == null) {
				val = Integer.parseInt(s);
			}
			num = add ? (num + val) : (num - val);
		}
		return num;
	}

}
