package com.charrey.algorithms;

import com.charrey.util.GraphUtil;
import com.charrey.graph.Vertex;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class CompatibilityChecker {



    public static  Map<Vertex, Set<Vertex>> get(Graph<Vertex, DefaultEdge> source,
                                                Graph<Vertex, DefaultEdge> target) {

        Map<Vertex, Set<Vertex>> res = new HashMap<>();
        for (Vertex v : source.vertexSet()) {
            res.put(v, new HashSet<>(target.vertexSet().stream().filter(x -> isCompatible(v, x, source, target)).collect(Collectors.toSet())));
        }
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = filterNeighbourHoods(res, source, target);
            hasChanged = hasChanged || filterGAC(res);
        }
        return res;
    }

    private static boolean filterGAC(Map<Vertex, Set<Vertex>> compatibility) {
        BiMap<Vertex, Integer> map1 = HashBiMap.create();
        for (Vertex i : compatibility.keySet()) {
            map1.put(i, i.intData());
        }
        BiMap<Vertex, Integer> map2 = HashBiMap.create();
        for (Set<Vertex> iset : compatibility.values()) {
            for (Vertex i : iset) {
                map2.put(i, i.getIntId());
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

    private static boolean filterNeighbourHoods(Map<Vertex, Set<Vertex>> compatibility,
                                                                                                   Graph<Vertex, DefaultEdge> source,
                                                                                                   Graph<Vertex, DefaultEdge> target) {
        boolean res = false;
        Set<Pair<Vertex, Vertex>> toRemove = new HashSet<>();
        for (Map.Entry<Vertex, Set<Vertex>> s : compatibility.entrySet()) {
            for (Vertex t : s.getValue()) {
                Set<Vertex> sourceNeighbourHood = GraphUtil.floodNeighboursOf(source, s.getKey(), x -> x.containsLabel("routing"));
                Set<Vertex> targetNeighbourHood = GraphUtil.floodNeighboursOf(target, t, x -> x.containsLabel("routing"));
                if (!compatibleNeighbourhoods(sourceNeighbourHood, targetNeighbourHood, compatibility)) {
                    res = true;
                    toRemove.add(new Pair<>(s.getKey(), t));
                }
            }
        }

        for (Pair<Vertex, Vertex> pair : toRemove) {
            compatibility.get(pair.getFirst()).remove(pair.getSecond());
        }
        return res;
    }

    private static  boolean compatibleNeighbourhoods(Set<Vertex> sources, Set<Vertex> targets, Map<Vertex, Set<Vertex>> compatibility) {
        Map<Vertex, Set<Vertex>> allDifferentMap = new HashMap<>();
        for (Map.Entry<Vertex, Set<Vertex>> entry : compatibility.entrySet()) {
           if (sources.contains(entry.getKey())) {
                allDifferentMap.put(entry.getKey(), entry.getValue()
                        .stream()
                        .filter(targets::contains)
                        .collect(Collectors.toSet()));
            }
        }
        return AllDifferent.get(new LinkedList<>(sources), allDifferentMap, Vertex::getIntId);
    }

    private static <T extends Comparable<T>> boolean isCompatible(Vertex sourceVertex, Vertex targetVertex, Graph<Vertex, DefaultEdge> source, Graph<Vertex, DefaultEdge> target) {
        return source.degreeOf(sourceVertex) <= target.degreeOf(targetVertex) &&
                targetVertex.getLabels().containsAll(sourceVertex.getLabels());
    }
}
