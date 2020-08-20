package com.charrey.algorithms;

import com.charrey.graph.MyGraph;
import com.charrey.util.GraphUtil;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class that provides a target graph domain for each source graph vertex.
 */
public class CompatibilityChecker {

    private final AllDifferent alldiff = new AllDifferent();

    /**
     * Returns a map from source graph vertices to the target graph vertices they are compatible with.
     *
     * @param source                 the source graph
     * @param target                 the target graph
     * @param neighbourhoodFiltering whether to filter the domains of each vertex v such that all candidates have neighbourhoods that can emulate v's neighbourhood.
     * @return the map
     */
    @NotNull
    public TIntObjectMap<TIntSet> get(@NotNull MyGraph source,
                                      @NotNull MyGraph target, boolean neighbourhoodFiltering) {

        TIntObjectMap<TIntSet> res = new TIntObjectHashMap<>();
        for (Integer v : source.vertexSet()) {
            res.put(v, new TIntHashSet(target.vertexSet().stream().filter(x -> isCompatible(v, x, source, target)).collect(Collectors.toSet())));
        }
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = false;
            if (neighbourhoodFiltering) {
                hasChanged = filterNeighbourHoods(res, source, target);
            }
        }
        return res;
    }

    private boolean filterNeighbourHoods(@NotNull TIntObjectMap<TIntSet> compatibilityMap,
                                         @NotNull MyGraph sourceGraph,
                                         @NotNull MyGraph targetGraph) {
        final boolean[] changed = {false};
        Set<Pair<Integer, Integer>> toRemove = new HashSet<>();
        final long[] lastTimePrinted = {System.currentTimeMillis()};

        compatibilityMap.forEachEntry((key, values1) -> {
            values1.forEach(potentialTarget -> {
                if (System.currentTimeMillis() - lastTimePrinted[0] > 1000) {
                    lastTimePrinted[0] = System.currentTimeMillis();
                }
                TIntSet sourceNeighbourHood = GraphUtil.reachableNeighbours(sourceGraph, key);
                TIntSet targetNeighbourHood = GraphUtil.reachableNeighbours(targetGraph, potentialTarget);
                if (!compatibleNeighbourhoods(sourceNeighbourHood, targetNeighbourHood, compatibilityMap)) {
                    changed[0] = true;
                    toRemove.add(new Pair<>(key, potentialTarget));
                }
                return true;
            });
            return true;
        });
        for (Pair<Integer, Integer> pair : toRemove) {
            compatibilityMap.get(pair.getFirst()).remove(pair.getSecond());
        }
        return changed[0];
    }

    private boolean compatibleNeighbourhoods(@NotNull TIntSet sourceNeighbourhood, @NotNull TIntSet targetNeighbourhood, @NotNull TIntObjectMap<TIntSet> compatibilityMap) {
        TIntObjectMap<TIntSet> allDifferentMap = new TIntObjectHashMap<>();
        compatibilityMap.forEachEntry((key, values) -> {
            if (sourceNeighbourhood.contains(key)) {
                TIntSet toPut = new TIntHashSet();
                values.forEach(i -> {
                    if (targetNeighbourhood.contains(i)) {
                        toPut.add(i);
                    }
                    return true;
                });
                allDifferentMap.put(key, toPut);
            }

            return true;
        });
        return alldiff.get(allDifferentMap);
    }

    private static boolean isCompatible(int sourceVertex, int targetVertex, @NotNull MyGraph sourceGraph, @NotNull MyGraph targetGraph) {
        assert sourceGraph.containsVertex(sourceVertex);
        assert targetGraph.containsVertex(targetVertex);
        return sourceGraph.degreeOf(sourceVertex) <= targetGraph.degreeOf(targetVertex) &&
                targetGraph.getLabels(targetVertex).containsAll(sourceGraph.getLabels(sourceVertex));
    }
}
