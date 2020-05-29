package com.charrey.algorithms;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.settings.Settings;
import com.charrey.util.GraphUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jgrapht.alg.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CompatibilityChecker {

    private final AllDifferent alldiff = new AllDifferent();

    public  Map<Vertex, Set<Vertex>> get(MyGraph source,
                                         MyGraph target) {

        Map<Vertex, Set<Vertex>> res = new HashMap<>();
        for (Vertex v : source.vertexSet()) {
            assert source.containsVertex(v);
            res.put(v, new HashSet<>(target.vertexSet().stream().filter(x -> isCompatible(v, x, source, target)).collect(Collectors.toSet())));
        }
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = false;
            if (Settings.instance.initialLocalizedAllDifferent) {
                hasChanged = filterNeighbourHoods(res, source, target);
            }
            if (Settings.instance.initialGlobalAllDifferent) {
                hasChanged = hasChanged || filterGAC(res);
            }
        }
        return res;
    }

    private static boolean filterGAC(Map<Vertex, Set<Vertex>> compatibility) {
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

    private boolean filterNeighbourHoods(Map<Vertex, Set<Vertex>> compatibilityMap,
                                         MyGraph sourceGraph,
                                         MyGraph targetGraph) {
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

    private boolean compatibleNeighbourhoods(Set<Vertex> sourceNeighbourhood, Set<Vertex> targetNeighbourhood, Map<Vertex, Set<Vertex>> compatibilityMap) {
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

    private static boolean isCompatible(Vertex sourceVertex, Vertex targetVertex, MyGraph sourceGraph, MyGraph targetGraph) {
        assert sourceGraph.containsVertex(sourceVertex);
        assert targetGraph.containsVertex(targetVertex);
        return sourceGraph.degreeOf(sourceVertex) <= targetGraph.degreeOf(targetVertex) &&
                targetVertex.getLabels().containsAll(sourceVertex.getLabels());
    }
}
