package com.charrey.algorithms;

import com.charrey.graph.MyGraph;
import com.charrey.util.GraphUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that provides a target graph domain for each source graph vertex.
 */
public class CompatibilityChecker {

    private final AllDifferent alldiff = new AllDifferent();

    private static boolean filterGAC(@NotNull Map<Integer, Set<Integer>> compatibility, int iteration, String name) {
        BiMap<Integer, Integer> map1 = HashBiMap.create();
        for (int i : compatibility.keySet()) {
            map1.put(i, i);
        }
        BiMap<Integer, Integer> map2 = HashBiMap.create();
        for (Set<Integer> iset : compatibility.values()) {
            for (int i : iset) {
                map2.put(i, i);
            }
        }

        Map<Integer, Set<Integer>> intCompatability = compatibility.entrySet().stream().collect(Collectors.toMap(x -> map1.get(x.getKey()), x -> x.getValue().stream().map(map2::get).collect(Collectors.toSet())));

        Set<Pair<Integer, Integer>> toRemoveInt = AllDifferent.checkAll(intCompatability, iteration, name);
        Set<Pair<Integer, Integer>> toRemove = toRemoveInt.stream()
                .map(x -> new Pair<>(map1.inverse().get(x.getFirst()), map2.inverse().get(x.getSecond())))
                .collect(Collectors.toSet());
        if (toRemove.isEmpty()) {
            return false;
        } else {
            for (Pair<Integer, Integer> pairToRemove : toRemove) {
                compatibility.get(pairToRemove.getFirst()).remove(pairToRemove.getSecond());
            }
            return true;
        }
    }

    /**
     * Returns a map from source graph vertices to the target graph vertices they are compatible with.
     *
     * @param source                    the source graph
     * @param target                    the target graph
     * @param neighbourhoodFiltering    whether to filter the domains of each vertex v such that all candidates have neighbourhoods that can emulate v's neighbourhood.
     * @param initialGlobalAllDifferent whether to apply AllDifferent to each possible matching to filter out candidates.
     * @return the map
     */
    @NotNull
    public Map<Integer, Set<Integer>> get(@NotNull MyGraph source,
                                          @NotNull MyGraph target, boolean neighbourhoodFiltering, boolean initialGlobalAllDifferent, String name) {

        Map<Integer, Set<Integer>> res = new HashMap<>();
        for (Integer v : source.vertexSet()) {
            assert source.containsVertex(v);
            res.put(v, new HashSet<>(target.vertexSet().stream().filter(x -> isCompatible(v, x, source, target)).collect(Collectors.toSet())));
        }
        boolean hasChanged = true;
        int iteration = 1;
        while (hasChanged) {
            hasChanged = false;
            if (neighbourhoodFiltering) {
                hasChanged = filterNeighbourHoods(res, source, target, iteration, name);
                System.out.println(name + "completed neighbourhood filtering");
            }
            if (initialGlobalAllDifferent) {
                hasChanged = hasChanged || filterGAC(res, iteration, name);
                System.out.println(name + "completed AllDifferent filtering");
            }
            iteration++;
        }
        return res;
    }

    private boolean filterNeighbourHoods(@NotNull Map<Integer, Set<Integer>> compatibilityMap,
                                         @NotNull MyGraph sourceGraph,
                                         @NotNull MyGraph targetGraph,
                                         int iteration,
                                         String name) {
        boolean changed = false;
        Set<Pair<Integer, Integer>> toRemove = new HashSet<>();

        long toProcess = 0L;
        Collection<Set<Integer>> values = compatibilityMap.values();
        for (Set<Integer> value : values) {
            toProcess = toProcess + value.size();
        }

        long lastTimePrinted = System.currentTimeMillis();
        long counter = 0;
        for (Map.Entry<Integer, Set<Integer>> potentialSource : compatibilityMap.entrySet()) {
            for (Integer potentialTarget : potentialSource.getValue()) {
                if (System.currentTimeMillis() - lastTimePrinted > 1000) {
                    System.out.println(name + " filtering neighbourhoods at iteration " + iteration + ": " + 100 * counter / (double) toProcess + "%");
                    lastTimePrinted = System.currentTimeMillis();
                }
                Set<Integer> sourceNeighbourHood = GraphUtil.reachableNeighbours(sourceGraph, potentialSource.getKey());
                Set<Integer> targetNeighbourHood = GraphUtil.reachableNeighbours(targetGraph, potentialTarget);
                if (!compatibleNeighbourhoods(sourceNeighbourHood, targetNeighbourHood, compatibilityMap)) {
                    changed = true;
                    toRemove.add(new Pair<>(potentialSource.getKey(), potentialTarget));
                }
                counter++;
            }
        }
        for (Pair<Integer, Integer> pair : toRemove) {
            compatibilityMap.get(pair.getFirst()).remove(pair.getSecond());
        }
        return changed;
    }

    private boolean compatibleNeighbourhoods(@NotNull Set<Integer> sourceNeighbourhood, @NotNull Set<Integer> targetNeighbourhood, @NotNull Map<Integer, Set<Integer>> compatibilityMap) {
        Map<Integer, Set<Integer>> allDifferentMap = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : compatibilityMap.entrySet()) {
           if (sourceNeighbourhood.contains(entry.getKey())) {
                allDifferentMap.put(entry.getKey(), entry.getValue()
                        .stream()
                        .filter(targetNeighbourhood::contains)
                        .collect(Collectors.toSet()));
            }
        }
        return alldiff.get(allDifferentMap);
    }

    private static boolean isCompatible(int sourceVertex, int targetVertex, @NotNull MyGraph sourceGraph, @NotNull MyGraph targetGraph) {
        assert sourceGraph.containsVertex(sourceVertex);
        assert targetGraph.containsVertex(targetVertex);
        return sourceGraph.degreeOf(sourceVertex) <= targetGraph.degreeOf(targetVertex) &&
                targetGraph.getLabels(targetVertex).containsAll(sourceGraph.getLabels(sourceVertex));
    }
}
