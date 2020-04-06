package com.charrey.algorithms;

import com.charrey.util.GraphUtil;
import com.charrey.graph.AttributedVertex;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class CompatibilityChecker {



    public static  Map<AttributedVertex, Set<AttributedVertex>> get(Graph<AttributedVertex, DefaultEdge> source,
                                                                                                           Graph<AttributedVertex, DefaultEdge> target) {

        Map<AttributedVertex, Set<AttributedVertex>> res = new HashMap<>();
        for (AttributedVertex v : source.vertexSet()) {
            res.put(v, new HashSet<>(target.vertexSet().stream().filter(x -> isCompatible(v, x, source, target)).collect(Collectors.toSet())));
        }
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = filterNeighbourHoods(res, source, target);
            hasChanged = hasChanged || filterGAC(res);
        }
        return res;
    }

    private static boolean filterGAC(Map<AttributedVertex, Set<AttributedVertex>> compatibility) {
        BiMap<AttributedVertex, Integer> map1 = HashBiMap.create();
        for (AttributedVertex i : compatibility.keySet()) {
            map1.put(i, i.intData());
        }
        BiMap<AttributedVertex, Integer> map2 = HashBiMap.create();
        for (Set<AttributedVertex> iset : compatibility.values()) {
            for (AttributedVertex i : iset) {
                map2.put(i, i.getIntId());
            }
        }

        Map<Integer, Set<Integer>> intCompatability = compatibility.entrySet().stream().collect(Collectors.toMap(x -> map1.get(x.getKey()), x -> x.getValue().stream().map(map2::get).collect(Collectors.toSet())));

        Set<Pair<Integer, Integer>> toRemoveInt = AllDifferent.checkAll(intCompatability);
        Set<Pair<AttributedVertex, AttributedVertex>> toRemove = toRemoveInt.stream()
                .map(x -> new Pair<>(map1.inverse().get(x.getFirst()), map2.inverse().get(x.getSecond())))
                .collect(Collectors.toSet());
        if (toRemove.isEmpty()) {
            return false;
        } else {
            for (Pair<AttributedVertex, AttributedVertex> pairToRemove : toRemove) {
                compatibility.get(pairToRemove.getFirst()).remove(pairToRemove.getSecond());
            }
            return true;
        }
    }

    private static boolean filterNeighbourHoods(Map<AttributedVertex, Set<AttributedVertex>> compatibility,
                                                                                                   Graph<AttributedVertex, DefaultEdge> source,
                                                                                                   Graph<AttributedVertex, DefaultEdge> target) {
        boolean res = false;
        Set<Pair<AttributedVertex, AttributedVertex>> toRemove = new HashSet<>();
        for (Map.Entry<AttributedVertex, Set<AttributedVertex>> s : compatibility.entrySet()) {
            for (AttributedVertex t : s.getValue()) {
                Set<AttributedVertex> sourceNeighbourHood = GraphUtil.floodNeighboursOf(source, s.getKey(), x -> x.containsLabel("routing"));
                Set<AttributedVertex> targetNeighbourHood = GraphUtil.floodNeighboursOf(target, t, x -> x.containsLabel("routing"));
                if (!compatibleNeighbourhoods(sourceNeighbourHood, targetNeighbourHood, compatibility)) {
                    res = true;
                    toRemove.add(new Pair<>(s.getKey(), t));
                }
            }
        }

        for (Pair<AttributedVertex, AttributedVertex> pair : toRemove) {
            compatibility.get(pair.getFirst()).remove(pair.getSecond());
        }
        return res;
    }

    private static  boolean compatibleNeighbourhoods(Set<AttributedVertex> sources, Set<AttributedVertex> targets, Map<AttributedVertex, Set<AttributedVertex>> compatibility) {
        Map<AttributedVertex, Set<AttributedVertex>> allDifferentMap = new HashMap<>();
        for (Map.Entry<AttributedVertex, Set<AttributedVertex>> entry : compatibility.entrySet()) {
           if (sources.contains(entry.getKey())) {
                allDifferentMap.put(entry.getKey(), entry.getValue()
                        .stream()
                        .filter(targets::contains)
                        .collect(Collectors.toSet()));
            }
        }
        return AllDifferent.get(new LinkedList<>(sources), allDifferentMap, AttributedVertex::getIntId);
    }

    private static <T extends Comparable<T>> boolean isCompatible(AttributedVertex sourceVertex, AttributedVertex targetVertex, Graph<AttributedVertex, DefaultEdge> source, Graph<AttributedVertex, DefaultEdge> target) {
        return source.degreeOf(sourceVertex) <= target.degreeOf(targetVertex) &&
                targetVertex.getLabels().containsAll(sourceVertex.getLabels());
    }
}
