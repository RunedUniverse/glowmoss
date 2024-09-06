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

import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Logger;

import net.runeduniverse.tools.glowmoss.modes.ExecMode;
import net.runeduniverse.tools.glowmoss.modes.FindMode;
import net.runeduniverse.tools.glowmoss.modes.ImportMode;
import net.runeduniverse.tools.glowmoss.options.InvalidArgumentException;
import net.runeduniverse.tools.glowmoss.options.MissingOptionException;
import net.runeduniverse.tools.glowmoss.options.Options;

import static net.runeduniverse.tools.glowmoss.ArgUtils.collectArgs;

public class Launcher {

	private static final Set<ExecMode> loadedModes = new LinkedHashSet<>();

	private static ConsoleLogger logger = new ConsoleLogger(Logger.getLogger(Launcher.class.getName()));
	private static Options options = new Options();
	private static ExecMode mode = null;

	public static void main(String[] args) {
		RogmPatches.patch();

		registerExecModes();

		try {
			init(args);
		} catch (InvalidArgumentException | MissingOptionException e) {
			final String msg = e.getMessage();
			if (msg != null)
				System.err.println("ERR: " + e.getMessage());
			System.err.println("Invalid arguments detected, stopping!");
			return;
		}

	}

	private static void registerExecModes() {
		loadedModes.add(new ImportMode());
		loadedModes.add(new FindMode());
	}

	public static boolean init(String[] argArr) throws InvalidArgumentException, MissingOptionException {
		argLoop: for (ListIterator<String> it = collectArgs(argArr).listIterator(); it.hasNext();) {
			if (mode == null) {
				for (ExecMode execMode : loadedModes)
					if (execMode.handle(it)) {
						mode = execMode;
						continue argLoop;
					}
				break argLoop;
			}

			options.init(it);
		}

		if (mode == null)
			throw new InvalidArgumentException("No Mode specified!");

		// eval required args
		mode.validate(logger, options);
		return true;
	}

}
