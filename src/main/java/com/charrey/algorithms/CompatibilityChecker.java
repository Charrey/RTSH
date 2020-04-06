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



    public static  <T extends Comparable<T>>  Map<AttributedVertex<Integer>, Set<AttributedVertex<T>>> get(Graph<AttributedVertex<Integer>, DefaultEdge> source,
                                                                                                           Graph<AttributedVertex<T>, DefaultEdge> target) {


        Map<AttributedVertex<Integer>, Set<AttributedVertex<T>>> res = new HashMap<>();
        for (AttributedVertex<Integer> v : source.vertexSet()) {
            res.put(v, new HashSet<>(target.vertexSet().stream().filter(x -> isCompatible(v, x, source, target)).collect(Collectors.toSet())));
        }
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = filterNeighbourHoods(res, source, target);
            hasChanged = hasChanged || filterGAC(res);
        }
        return res;
    }

    private static <T extends Comparable<T>> boolean filterGAC(Map<AttributedVertex<Integer>, Set<AttributedVertex<T>>> compatibility) {
        BiMap<AttributedVertex<Integer>, Integer> map1 = HashBiMap.create();
        for (AttributedVertex<Integer> i : compatibility.keySet()) {
            map1.put(i, i.id);
        }
        BiMap<AttributedVertex<T>, Integer> map2 = HashBiMap.create();
        for (Set<AttributedVertex<T>> iset : compatibility.values()) {
            for (AttributedVertex<T> i : iset) {
                map2.put(i, i.getIntId());
            }
        }

        Map<Integer, Set<Integer>> intCompatability = compatibility.entrySet().stream().collect(Collectors.toMap(x -> map1.get(x.getKey()), x -> x.getValue().stream().map(map2::get).collect(Collectors.toSet())));

        Set<Pair<Integer, Integer>> toRemoveInt = AllDifferent.checkAll(intCompatability);
        Set<Pair<AttributedVertex<Integer>, AttributedVertex<T>>> toRemove = toRemoveInt.stream()
                .map(x -> new Pair<>(map1.inverse().get(x.getFirst()), map2.inverse().get(x.getSecond())))
                .collect(Collectors.toSet());
        if (toRemove.isEmpty()) {
            return false;
        } else {
            for (Pair<AttributedVertex<Integer>, AttributedVertex<T>> pairToRemove : toRemove) {
                compatibility.get(pairToRemove.getFirst()).remove(pairToRemove.getSecond());
            }
            return true;
        }
    }

    private static <S extends Comparable<S>, T extends Comparable<T>> boolean filterNeighbourHoods(Map<AttributedVertex<S>, Set<AttributedVertex<T>>> compatibility,
                                                                                                   Graph<AttributedVertex<S>, DefaultEdge> source,
                                                                                                   Graph<AttributedVertex<T>, DefaultEdge> target) {
        boolean res = false;
        Set<Pair<AttributedVertex<S>, AttributedVertex<T>>> toRemove = new HashSet<>();
        for (Map.Entry<AttributedVertex<S>, Set<AttributedVertex<T>>> s : compatibility.entrySet()) {
            for (AttributedVertex<T> t : s.getValue()) {
                Set<AttributedVertex<S>> sourceNeighbourHood = GraphUtil.floodNeighboursOf(source, s.getKey(), x -> x.containsLabel("routing"));
                Set<AttributedVertex<T>> targetNeighbourHood = GraphUtil.floodNeighboursOf(target, t, x -> x.containsLabel("routing"));
                if (!compatibleNeighbourhoods(sourceNeighbourHood, targetNeighbourHood, compatibility)) {
                    res = true;
                    toRemove.add(new Pair<>(s.getKey(), t));
                }
            }
        }





        for (Pair<AttributedVertex<S>, AttributedVertex<T>> pair : toRemove) {
            compatibility.get(pair.getFirst()).remove(pair.getSecond());
        }
        return res;
    }

    private static <S extends Comparable<S>, T extends Comparable<T>> boolean compatibleNeighbourhoods(Set<AttributedVertex<S>> sources, Set<AttributedVertex<T>> targets, Map<AttributedVertex<S>, Set<AttributedVertex<T>>> compatibility) {
        Map<AttributedVertex<S>, Set<AttributedVertex<T>>> allDifferentMap = new HashMap<>();
        for (Map.Entry<AttributedVertex<S>, Set<AttributedVertex<T>>> entry : compatibility.entrySet()) {
           if (sources.contains(entry.getKey())) {
                allDifferentMap.put(entry.getKey(), entry.getValue()
                        .stream()
                        .filter(targets::contains)
                        .collect(Collectors.toSet()));
            }
        }
        return AllDifferent.get(new LinkedList<>(sources), allDifferentMap, AttributedVertex::getIntId);
    }

    private static <S extends Comparable<S>, T extends Comparable<T>> boolean isCompatible(AttributedVertex<S> sourceVertex, AttributedVertex<T> targetVertex, Graph<AttributedVertex<S>, DefaultEdge> source, Graph<AttributedVertex<T>, DefaultEdge> target) {
        return source.degreeOf(sourceVertex) <= target.degreeOf(targetVertex) &&
                targetVertex.getLabels().containsAll(sourceVertex.getLabels());
    }
}
