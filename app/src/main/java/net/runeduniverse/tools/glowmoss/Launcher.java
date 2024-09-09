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

import net.runeduniverse.tools.glowmoss.modules.ExecModule;
import net.runeduniverse.tools.glowmoss.modules.FirewallModule;
import net.runeduniverse.tools.glowmoss.modules.ImportModule;
import net.runeduniverse.tools.glowmoss.options.InvalidArgumentException;
import net.runeduniverse.tools.glowmoss.options.MissingOptionException;
import net.runeduniverse.tools.glowmoss.options.Options;

import static net.runeduniverse.tools.glowmoss.ArgUtils.collectArgs;

public class Launcher {

	private static final Set<ExecModule> loadedModes = new LinkedHashSet<>();

	private static ConsoleLogger logger = new ConsoleLogger(Logger.getLogger(Launcher.class.getName()));
	private static Options options = new Options();
	private static ExecModule module = null;

	public static void main(String[] args) {
		RogmPatches.patch();

		registerExecModes();

		try {
			if (!init(args))
				return;
		} catch (InvalidArgumentException | MissingOptionException e) {
			final String msg = e.getMessage();
			if (msg != null)
				System.err.println("ERR: " + e.getMessage());
			System.err.println("Invalid arguments detected, stopping!");
			return;
		}

		module.exec(logger, options);
	}

	private static void registerExecModes() {
		loadedModes.add(new ImportModule());
		loadedModes.add(new FirewallModule());
	}

	public static boolean init(String[] argArr) throws InvalidArgumentException, MissingOptionException {
		final ListIterator<String> it = collectArgs(argArr).listIterator();
		argLoop: while (it.hasNext()) {
			if (module == null) {
				for (ExecModule execMode : loadedModes)
					if (execMode.handle(it)) {
						module = execMode;
						continue argLoop;
					}
				break argLoop;
			}

			options.init(it);
		}

		if (module == null) {
			// parse simple options
			while (it.hasNext())
				options.handle(it);
			// handle call for help
			if (options.help()) {
				help();
				return false;
			}
			// complain
			throw new InvalidArgumentException("No Module specified!");
		}

		if (options.help()) {
			module.help(logger, options);
			return false;
		}

		// eval required args
		module.validate(logger, options);
		return true;
	}

	public static void help() {
		System.out.println(">> Glowmoss");
		System.out.println("    import --help");
		System.out.println("    firewall < match > --help");
		System.out.println();
	}

}
