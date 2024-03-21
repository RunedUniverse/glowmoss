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

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class ConsoleLogger extends Logger {

	private static final ConsoleHandler CONSOLE_HANDLER = new ConsoleHandler();

	static {
		CONSOLE_HANDLER.setLevel(Level.ALL);
	}

	public ConsoleLogger() {
		super("ROGM-DEBUG-CONSOLE", null);
		super.setLevel(Level.ALL);
		super.addHandler(CONSOLE_HANDLER);
	}

	public ConsoleLogger(Logger parent) {
		this();
		super.setParent(parent);
	}

	@Override
	public void log(LogRecord record) {
		System.out.println("[LOGGING-CHAIN-OVERRIDE][" + record.getLevel()
				.getName() + "]\n" + record.getMessage());
		super.log(record);
	}
}
