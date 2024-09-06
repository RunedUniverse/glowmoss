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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.runeduniverse.lib.utils.common.StringUtils;

public class ArgUtils {

	public static List<String> collectArgs(String[] argArr) {
		final List<String> entries = new LinkedList<>();
		StringBuilder entry = null;
		boolean txtActive = false;
		for (Iterator<String> it = Arrays.asList(argArr)
				.iterator(); it.hasNext();) {
			String arg = it.next();
			if (entry == null)
				entry = new StringBuilder();

			if (txtActive) {
				// append space removed by splitting
				entry.append(' ');
			} else {
				// skip blanks
				if (StringUtils.isBlank(arg))
					continue;
			}

			entry.append(arg);

			for (int idxQuote = arg.indexOf('"'); -1 < idxQuote; idxQuote = arg.indexOf('"', idxQuote + 1)) {
				if (countIdentCharBeforeIdx(arg, '\\', idxQuote) % 2 == 0) {
					// quote is active => switch state
					txtActive = !txtActive;
				}
			}

			if (!txtActive) {
				entries.add(entry.toString());
				entry = null;
			}
		}
		if (entry != null && entry.length() != 0) {
			entries.add(entry.toString());
		}
		return entries;
	}

	public static int countIdentCharBeforeIdx(CharSequence txt, char c, int idx) {
		int cnt = 0;
		if (idx < 1 || txt == null || txt.length() < idx)
			return cnt;
		for (int i = idx - 1; 0 <= i; i = i - 1) {
			if (txt.charAt(i) == c)
				cnt = cnt + 1;
			else
				break;
		}
		return cnt;
	}

}
