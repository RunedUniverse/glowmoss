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
package net.runeduniverse.tools.glowmoss.model.firewall;

public enum Hook {

	INGRESS {
		@Override
		boolean isAvailable(Family family, ChainType type) {
			switch (family) {
			case INET:
			case NETDEV:
				if (ChainType.FILTER.equals(type))
					return true;
			default:
				return false;
			}
		}
	},
	PREROUTING {
		@Override
		boolean isAvailable(Family family, ChainType type) {
			switch (family) {
			case INET:
			case IP6:
			case IP:
				if (ChainType.NAT.equals(type))
					return true;
			case BRIDGE:
				return ChainType.FILTER.equals(type);
			default:
				return false;
			}
		}
	},
	FORWARD {
		@Override
		boolean isAvailable(Family family, ChainType type) {
			switch (family) {
			case INET:
			case IP6:
			case IP:
			case BRIDGE:
				return ChainType.FILTER.equals(type);
			default:
				return false;
			}
		}
	},
	INPUT {
		@Override
		boolean isAvailable(Family family, ChainType type) {
			switch (family) {
			case INET:
			case IP6:
			case IP:
				if (ChainType.NAT.equals(type))
					return true;
			case ARP:
			case BRIDGE:
				return ChainType.FILTER.equals(type);
			default:
				return false;
			}
		}
	},
	OUTPUT {
		@Override
		boolean isAvailable(Family family, ChainType type) {
			switch (family) {
			case INET:
			case IP6:
			case IP:
				if (ChainType.NAT.equals(type) || ChainType.ROUTE.equals(type))
					return true;
			case ARP:
			case BRIDGE:
				return ChainType.FILTER.equals(type);
			default:
				return false;
			}
		}
	},
	POSTROUTING {
		@Override
		boolean isAvailable(Family family, ChainType type) {
			switch (family) {
			case INET:
			case IP6:
			case IP:
				if (ChainType.NAT.equals(type))
					return true;
			case BRIDGE:
				return ChainType.FILTER.equals(type);
			default:
				return false;
			}
		}
	},
	EGRESS {
		@Override
		boolean isAvailable(Family family, ChainType type) {
			return Family.NETDEV.equals(family) && ChainType.FILTER.equals(type);
		}
	};

	abstract boolean isAvailable(Family family, ChainType type);

}
