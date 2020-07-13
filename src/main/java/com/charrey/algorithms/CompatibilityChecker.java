package com.charrey.algorithms;

import com.charrey.graph.MyGraph;
import com.charrey.util.GraphUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class that provides a target graph domain for each source graph vertex.
 */
public class CompatibilityChecker {

    private final AllDifferent alldiff = new AllDifferent();

    private static boolean filterGAC(@NotNull Map<Integer, TIntSet> compatibility, int iteration, String name) {
        BiMap<Integer, Integer> map1 = HashBiMap.create();
        for (int i : compatibility.keySet()) {
            map1.put(i, i);
        }
        BiMap<Integer, Integer> map2 = HashBiMap.create();
        for (TIntSet iset : compatibility.values()) {
            iset.forEach(i -> {
                map2.put(i, i);
                return true;
            });

        }

        TIntObjectMap<TIntSet> intCompatibility = new TIntObjectHashMap<>();
        for (Map.Entry<Integer, TIntSet> entry : compatibility.entrySet()) {
            TIntSet toPut = new TIntHashSet();
            entry.getValue().forEach(i -> {
                toPut.add(i);
                return true;
            });
            intCompatibility.put(entry.getKey(), toPut);
        }

        Set<int[]> toRemoveInt = AllDifferent.checkAll(intCompatibility, iteration, name);
        Set<int[]> toRemove = toRemoveInt.stream()
                .map(x -> new int[]{map1.inverse().get(x[0]), map2.inverse().get(x[1])})
                .collect(Collectors.toSet());
        if (toRemove.isEmpty()) {
            return false;
        } else {
            for (int[] pairToRemove : toRemove) {
                compatibility.get(pairToRemove[0]).remove(pairToRemove[1]);
            }
            return true;
        }
    }

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
                                      @NotNull MyGraph target, boolean neighbourhoodFiltering, String name) {

        TIntObjectMap<TIntSet> res = new TIntObjectHashMap<>();
        for (Integer v : source.vertexSet()) {
            assert source.containsVertex(v);
            res.put(v, new TIntHashSet(target.vertexSet().stream().filter(x -> isCompatible(v, x, source, target)).collect(Collectors.toSet())));
        }
        boolean hasChanged = true;
        int iteration = 1;
        while (hasChanged) {
            hasChanged = false;
            if (neighbourhoodFiltering) {
                hasChanged = filterNeighbourHoods(res, source, target, iteration, name);
                //System.out.println(name + "completed neighbourhood filtering");
            }
            iteration++;
        }
        return res;
    }

    private boolean filterNeighbourHoods(@NotNull TIntObjectMap<TIntSet> compatibilityMap,
                                         @NotNull MyGraph sourceGraph,
                                         @NotNull MyGraph targetGraph,
                                         int iteration,
                                         String name) {
        final boolean[] changed = {false};
        Set<Pair<Integer, Integer>> toRemove = new HashSet<>();

        final long[] toProcess = {0L};
        Collection<TIntSet> values = compatibilityMap.valueCollection();
        values.forEach(tIntSet -> toProcess[0] = toProcess[0] + tIntSet.size());

        final long[] lastTimePrinted = {System.currentTimeMillis()};
        final long[] counter = {0};

        compatibilityMap.forEachEntry((key, values1) -> {
            values1.forEach(potentialTarget -> {
                if (System.currentTimeMillis() - lastTimePrinted[0] > 1000) {
                    //System.out.println(name + " filtering neighbourhoods at iteration " + iteration + ": " + 100 * counter / (double) toProcess + "%");
                    lastTimePrinted[0] = System.currentTimeMillis();
                }
                TIntSet sourceNeighbourHood = GraphUtil.reachableNeighbours(sourceGraph, key);
                TIntSet targetNeighbourHood = GraphUtil.reachableNeighbours(targetGraph, potentialTarget);
                if (!compatibleNeighbourhoods(sourceNeighbourHood, targetNeighbourHood, compatibilityMap)) {
                    changed[0] = true;
                    toRemove.add(new Pair<>(key, potentialTarget));
                }
                counter[0]++;
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
