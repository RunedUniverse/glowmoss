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
package net.runeduniverse.tools.glowmoss.options;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ListIterator;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class NFTablesOptions {

	// PARSER: NFTables
	@Getter
	private Path ruleset = null;

	public boolean handle(ListIterator<String> argPtr) throws InvalidArgumentException {
		switch (argPtr.next()) {

		case "--nft-ruleset":
			if (!argPtr.hasNext()) {
				throw new InvalidArgumentException("Missing value: --nft-ruleset <path/to/ruleset>");
			}
			String val = argPtr.next();
			try {
				this.ruleset = Paths.get(val);
			} catch (InvalidPathException e) {
				throw new InvalidArgumentException("Invalid nft ruleset path: " + val);
			}
			return true;
		}
		// reset prt if no match was found!
		argPtr.previous();
		return false;
	}

	public NFTablesOptions requireRuleset() throws MissingOptionException {
		if (this.ruleset == null)
			throw new MissingOptionException("Missing option: --nft-ruleset <path/to/ruleset>");
		if (!Files.isReadable(this.ruleset))
			throw new MissingOptionException("File can not be accessed: --nft-ruleset " + this.ruleset.toString());
		return this;
	}

}
