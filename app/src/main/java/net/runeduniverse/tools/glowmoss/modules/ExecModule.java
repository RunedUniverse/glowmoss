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
package net.runeduniverse.tools.glowmoss.modules;

import java.util.ListIterator;

import net.runeduniverse.tools.glowmoss.ConsoleLogger;
import net.runeduniverse.tools.glowmoss.options.InvalidArgumentException;
import net.runeduniverse.tools.glowmoss.options.MissingOptionException;
import net.runeduniverse.tools.glowmoss.options.Options;

public interface ExecModule {

	public boolean handle(ListIterator<String> argPtr) throws InvalidArgumentException;

	public void help(ConsoleLogger logger, Options options);

	public void validate(ConsoleLogger logger, Options options) throws MissingOptionException;

	public boolean exec(ConsoleLogger logger, Options options);

}
