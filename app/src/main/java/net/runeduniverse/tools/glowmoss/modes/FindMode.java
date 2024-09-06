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
package net.runeduniverse.tools.glowmoss.modes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ListIterator;

import net.runeduniverse.tools.glowmoss.ConsoleLogger;
import net.runeduniverse.tools.glowmoss.model.firewall.Firewall;
import net.runeduniverse.tools.glowmoss.options.InvalidArgumentException;
import net.runeduniverse.tools.glowmoss.options.MissingOptionException;
import net.runeduniverse.tools.glowmoss.options.Options;
import net.runeduniverse.tools.glowmoss.parser.FirewallParser;

public class FindMode implements ExecMode {

	private Options options;

	@Override
	public boolean handle(ListIterator<String> argPtr) throws InvalidArgumentException {
		switch (argPtr.next()) {
		case "find":
			return true;
		}
		argPtr.previous();
		return false;
	}

	@Override
	public void validate(ConsoleLogger logger, Options options) throws MissingOptionException {
		options.nftOptions()
				.requireRuleset();
	}

	@Override
	public boolean exec(ConsoleLogger logger, Options options) {

		final Firewall firewall = parseFW();
		if (firewall == null) {
			logger.info("Failed to parse Firewall!");
			return false;
		}

		return true;
	}

	private Firewall parseFW() {
		Firewall firewall = Firewall.create();

		FirewallParser parser = new FirewallParser(firewall);

		Path ruleset = options.nftOptions()
				.ruleset();
		if (ruleset == null)
			return null;

		try {
			parser.parse(ruleset);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return firewall;
	}

}
