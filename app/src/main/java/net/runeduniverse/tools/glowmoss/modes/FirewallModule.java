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
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;

import net.runeduniverse.tools.glowmoss.ConsoleLogger;
import net.runeduniverse.tools.glowmoss.model.firewall.Chain;
import net.runeduniverse.tools.glowmoss.model.firewall.Firewall;
import net.runeduniverse.tools.glowmoss.model.firewall.Rule;
import net.runeduniverse.tools.glowmoss.model.firewall.Table;
import net.runeduniverse.tools.glowmoss.options.InvalidArgumentException;
import net.runeduniverse.tools.glowmoss.options.MatchOptions;
import net.runeduniverse.tools.glowmoss.options.MissingOptionException;
import net.runeduniverse.tools.glowmoss.options.Options;
import net.runeduniverse.tools.glowmoss.parser.FirewallParser;

public class FirewallModule implements ExecModule {

	private Options options;
	private boolean modeMatch = false;

	@Override
	public boolean handle(ListIterator<String> argPtr) throws InvalidArgumentException {
		switch (argPtr.next()) {
		case "firewall":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Module is missing arguments! : [match|?]");
			}

			switch (argPtr.next()) {
			case "match":
				modeMatch = true;
				return true;
			}
			// give another module the chance to load an extension
			argPtr.previous();
		}
		argPtr.previous();
		return false;
	}

	@Override
	public void help(ConsoleLogger logger, Options options) {
		System.out.println(">> Glowmoss");
		System.out.println("    firewall match --nft-ruleset <path/to/ruleset> --match-* <value> [...]");
		System.out.println();
	}

	@Override
	public void validate(ConsoleLogger logger, Options options) throws MissingOptionException {
		options.nftOptions()
				.requireRuleset();
	}

	@Override
	public boolean exec(ConsoleLogger logger, Options options) {
		this.options = options;

		final Firewall firewall = Firewall.create();
		final Set<Table> tables = parseFW(firewall);
		if (tables == null) {
			logger.info("Failed to parse Firewall!");
			return false;
		}

		if (this.modeMatch)
			execMatch(firewall, tables, this.options.matchOptions());

		return true;
	}

	private Set<Table> parseFW(Firewall firewall) {
		final FirewallParser parser = new FirewallParser(firewall);
		final Path ruleset = this.options.nftOptions()
				.ruleset();
		if (ruleset == null)
			return null;

		try {
			parser.parse(ruleset);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		final Set<Table> tables = new LinkedHashSet<>();
		tables.addAll(parser.getResults());
		return tables;
	}

	private void execMatch(Firewall firewall, Set<Table> tables, MatchOptions options) {

		for (Table table : tables) {

			for (Chain chain : table.getChains()) {

				for (Rule rule : chain.getRules()) {

				}

			}

		}

	}

}
