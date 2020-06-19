package com.charrey.algorithms;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.util.GraphUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
     * @param source                    the source graph
     * @param target                    the target graph
     * @param neighbourhoodFiltering    whether to filter the domains of each vertex v such that all candidates have neighbourhoods that can emulate v's neighbourhood.
     * @param initialGlobalAllDifferent whether to apply AllDifferent to each possible matching to filter out candidates.
     * @return the map
     */
    @NotNull
    public  Map<Vertex, Set<Vertex>> get(@NotNull MyGraph source,
                                         @NotNull MyGraph target, boolean neighbourhoodFiltering, boolean initialGlobalAllDifferent) {

        Map<Vertex, Set<Vertex>> res = new HashMap<>();
        for (Vertex v : source.vertexSet()) {
            assert source.containsVertex(v);
            res.put(v, new HashSet<>(target.vertexSet().stream().filter(x -> isCompatible(v, x, source, target)).collect(Collectors.toSet())));
        }
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = false;
            if (neighbourhoodFiltering) {
                hasChanged = filterNeighbourHoods(res, source, target);
            }
            if (initialGlobalAllDifferent) {
                hasChanged = hasChanged || filterGAC(res);
            }
        }
        return res;
    }

    private static boolean filterGAC(@NotNull Map<Vertex, Set<Vertex>> compatibility) {
        BiMap<Vertex, Integer> map1 = HashBiMap.create();
        for (Vertex i : compatibility.keySet()) {
            map1.put(i, i.data());
        }
        BiMap<Vertex, Integer> map2 = HashBiMap.create();
        for (Set<Vertex> iset : compatibility.values()) {
            for (Vertex i : iset) {
                map2.put(i, i.data());
            }
        }

        Map<Integer, Set<Integer>> intCompatability = compatibility.entrySet().stream().collect(Collectors.toMap(x -> map1.get(x.getKey()), x -> x.getValue().stream().map(map2::get).collect(Collectors.toSet())));

        Set<Pair<Integer, Integer>> toRemoveInt = AllDifferent.checkAll(intCompatability);
        Set<Pair<Vertex, Vertex>> toRemove = toRemoveInt.stream()
                .map(x -> new Pair<>(map1.inverse().get(x.getFirst()), map2.inverse().get(x.getSecond())))
                .collect(Collectors.toSet());
        if (toRemove.isEmpty()) {
            return false;
        } else {
            for (Pair<Vertex, Vertex> pairToRemove : toRemove) {
                compatibility.get(pairToRemove.getFirst()).remove(pairToRemove.getSecond());
            }
            return true;
        }
    }

    private boolean filterNeighbourHoods(@NotNull Map<Vertex, Set<Vertex>> compatibilityMap,
                                         @NotNull MyGraph sourceGraph,
                                         @NotNull MyGraph targetGraph) {
        boolean changed = false;
        Set<Pair<Vertex, Vertex>> toRemove = new HashSet<>();
        for (Map.Entry<Vertex, Set<Vertex>> potentialSource : compatibilityMap.entrySet()) {
            for (Vertex potentialTarget : potentialSource.getValue()) {
                Set<Vertex> sourceNeighbourHood = GraphUtil.reachableNeighbours(sourceGraph, potentialSource.getKey());
                Set<Vertex> targetNeighbourHood = GraphUtil.reachableNeighbours(targetGraph, potentialTarget);
                if (!compatibleNeighbourhoods(sourceNeighbourHood, targetNeighbourHood, compatibilityMap)) {
                    changed = true;
                    toRemove.add(new Pair<>(potentialSource.getKey(), potentialTarget));
                }
            }
        }
        for (Pair<Vertex, Vertex> pair : toRemove) {
            compatibilityMap.get(pair.getFirst()).remove(pair.getSecond());
        }
        return changed;
    }

    private boolean compatibleNeighbourhoods(@NotNull Set<Vertex> sourceNeighbourhood, @NotNull Set<Vertex> targetNeighbourhood, @NotNull Map<Vertex, Set<Vertex>> compatibilityMap) {
        Map<Vertex, Set<Vertex>> allDifferentMap = new HashMap<>();
        for (Map.Entry<Vertex, Set<Vertex>> entry : compatibilityMap.entrySet()) {
           if (sourceNeighbourhood.contains(entry.getKey())) {
                allDifferentMap.put(entry.getKey(), entry.getValue()
                        .stream()
                        .filter(targetNeighbourhood::contains)
                        .collect(Collectors.toSet()));
            }
        }
        return alldiff.get(allDifferentMap);
    }

    private static boolean isCompatible(@NotNull Vertex sourceVertex, @NotNull Vertex targetVertex, @NotNull MyGraph sourceGraph, @NotNull MyGraph targetGraph) {
        assert sourceGraph.containsVertex(sourceVertex);
        assert targetGraph.containsVertex(targetVertex);
        return sourceGraph.degreeOf(sourceVertex) <= targetGraph.degreeOf(targetVertex) &&
                targetVertex.getLabels().containsAll(sourceVertex.getLabels());
    }
}
