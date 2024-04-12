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

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import net.runeduniverse.lib.rogm.pattern.Archive;
import net.runeduniverse.lib.rogm.pattern.IBaseQueryPattern;
import net.runeduniverse.lib.rogm.pattern.IPattern;
import net.runeduniverse.lib.rogm.pattern.IPattern.IData;
import net.runeduniverse.lib.rogm.pipeline.chain.Chains;
import net.runeduniverse.lib.rogm.querying.QueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.NodeQueryBuilder;
import net.runeduniverse.lib.rogm.querying.QueryBuilder.RelationQueryBuilder;
import net.runeduniverse.lib.utils.chain.Chain;
import net.runeduniverse.lib.utils.chain.ChainManager;
import net.runeduniverse.lib.utils.chain.ChainRuntime;

public class RogmPatches {

	public static void patch() {
		QueryBuilder.CREATOR_NODE_BUILDER = RogmPatches::createNodeBuilder;
		QueryBuilder.CREATOR_REALATION_BUILDER = RogmPatches::createRelationBuilder;
	}

	public static void patch(ChainManager chainManager) {
		chainManager.addChainLayers(RogmPatches.class);
	}

	// helpers

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

	// CREATER

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

	// CHAINS

	@Chain(label = Chains.BUFFER_CHAIN.LOAD.LABEL, layers = { Chains.BUFFER_CHAIN.LOAD.PREPARE_DATA - 10 })
	public static IBaseQueryPattern<?> locateBestMatchByLabelsPattern(final ChainRuntime<?> runtime,
			final Archive archive, final IBaseQueryPattern<?> pattern, IData data) {

		final Set<String> labels = data.getLabels();
		final LinkedList<IPattern> patterns = new LinkedList<>(
				archive.getPatternsByLabels(labels, pattern.getType(), IBaseQueryPattern.class));

		patterns.remove(null);
		if (patterns.isEmpty())
			return pattern;

		patterns.sort(new Comparator<IPattern>() {
			@Override
			public int compare(IPattern p1, IPattern p2) {
				final Class<?> t1 = p1.getType(), t2 = p2.getType();
				if (t1.equals(t2))
					return 0;
				if (t1.isAssignableFrom(t2))
					return 1;
				if (t2.isAssignableFrom(t1))
					return -1;
				// they are not comparable ... so fallback
				return Integer.compareUnsigned(p1.hashCode(), p2.hashCode());
			}
		});

		return (IBaseQueryPattern<?>) patterns.getLast();
	}

	// optional

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
