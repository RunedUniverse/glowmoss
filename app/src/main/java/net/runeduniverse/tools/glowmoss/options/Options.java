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

import java.util.ListIterator;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class Options {

	// GENERAL
	private boolean log = false;
	private boolean debug = false;
	private boolean help = false;

	private DBOptions dbOptions = new DBOptions();
	private NFTablesOptions nftOptions = new NFTablesOptions();
	private MatchOptions matchOptions = new MatchOptions();

	public void init(ListIterator<String> argPtr) throws InvalidArgumentException {
		// if a handler detects sth skip the others!
		if (handle(argPtr) || this.dbOptions.handle(argPtr) || this.nftOptions.handle(argPtr)
				|| this.matchOptions.handle(argPtr))
			;
	}

	public boolean handle(ListIterator<String> argPtr) {
		switch (argPtr.next()) {
		case "--log":
			this.log = true;
			return true;
		case "--debug":
			this.debug = true;
			return true;
		case "--help":
			this.help = true;
			return true;
		}
		// reset prt if no match was found!
		argPtr.previous();
		return false;
	}

}
