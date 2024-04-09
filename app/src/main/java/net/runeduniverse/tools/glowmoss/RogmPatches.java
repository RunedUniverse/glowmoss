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
import java.util.Set;

import net.runeduniverse.lib.rogm.pattern.Archive;
import net.runeduniverse.lib.rogm.pattern.IPattern;
import net.runeduniverse.lib.rogm.querying.QueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.NodeQueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.RelationQueryBuilder;

public class RogmPatches {

	public static void patch() {
		QueryBuilder.CREATOR_NODE_BUILDER = RogmPatches::createNodeBuilder;
		QueryBuilder.CREATOR_REALATION_BUILDER = RogmPatches::createRelationBuilder;
	}

	private static Set<String> collectLabels(final Archive archive, final Class<?> type) {
		final Set<String> labels = new LinkedHashSet<>();
		Class<?> superType = type.getSuperclass();
		while (superType != null && !Object.class.equals(superType)) {
			for (IPattern p : archive.getPatterns(superType)) {
				labels.addAll(p.getLabels());
			}
			superType = superType.getSuperclass();
		}
		return labels;
	}

	private static NodeQueryBuilder createNodeBuilder(Archive archive) {
		return new NodeQueryBuilder(archive) {
			@Override
			public NodeQueryBuilder where(Class<?> type) {
				this.proxyFilter.addLabels(collectLabels(this.archive, type));
				return super.where(type);
			}
		};
	}

	private static RelationQueryBuilder createRelationBuilder(Archive archive) {
		return new RelationQueryBuilder(archive) {
			@Override
			public RelationQueryBuilder where(Class<?> type) {
				this.proxyFilter.addLabels(collectLabels(this.archive, type));
				return super.where(type);
			}
		};
	}

	private static void logPatterns(Archive archive, Class<?> type) {
		System.err.println("Patterns of " + type.getSimpleName());
		Set<IPattern> patterns = archive.getPatterns(type);
		for (IPattern pattern : patterns) {
			System.err.println(" - " + pattern.getType()
					.getSimpleName());
		}
		System.err.println();
	}

}
