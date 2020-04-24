package com.charrey.algorithms;

import com.charrey.graph.Vertex;
import com.charrey.util.GraphUtil;
import com.charrey.util.IndexMap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CompatibilityChecker {

    private final AllDifferent alldiff = new AllDifferent();

    public  Map<Vertex, Set<Vertex>> get(Graph<Vertex, DefaultEdge> source,
                                                Graph<Vertex, DefaultEdge> target) {

        Map<Vertex, Set<Vertex>> res = new HashMap<>();
        for (Vertex v : source.vertexSet()) {
            assert source.containsVertex(v);
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

    private boolean filterNeighbourHoods(Map<Vertex, Set<Vertex>> compatibility,
                                                                                                   Graph<Vertex, DefaultEdge> source,
                                                                                                   Graph<Vertex, DefaultEdge> target) {
        boolean res = false;
        Set<Pair<Vertex, Vertex>> toRemove = new HashSet<>();
        for (Map.Entry<Vertex, Set<Vertex>> s : compatibility.entrySet()) {
            for (Vertex t : s.getValue()) {
                Set<Vertex> sourceNeighbourHood = GraphUtil.reachableNeighbours(source, s.getKey());//source.outgoingEdgesOf(s.getKey()).stream().map(source::getEdgeTarget).collect(Collectors.toSet());
                Set<Vertex> targetNeighbourHood = GraphUtil.reachableNeighbours(target, t);//target.outgoingEdgesOf(t).stream().map(source::getEdgeTarget).collect(Collectors.toSet());
                if (!compatibleNeighbourhoods(source.vertexSet().size(), sourceNeighbourHood, targetNeighbourHood, compatibility)) {
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

    private boolean compatibleNeighbourhoods(int graphSize, Set<Vertex> sources, Set<Vertex> targets, Map<Vertex, Set<Vertex>> compatibility) {
        Map<Vertex, Set<Vertex>> allDifferentMap = new IndexMap<>(graphSize);
        for (Map.Entry<Vertex, Set<Vertex>> entry : compatibility.entrySet()) {
           if (sources.contains(entry.getKey())) {
                allDifferentMap.put(entry.getKey(), entry.getValue()
                        .stream()
                        .filter(targets::contains)
                        .collect(Collectors.toSet()));
            }
        }
        return alldiff.get(graphSize, allDifferentMap);
    }

    private static boolean isCompatible(Vertex sourceVertex, Vertex targetVertex, Graph<Vertex, DefaultEdge> source, Graph<Vertex, DefaultEdge> target) {
        assert source.containsVertex(sourceVertex);
        assert target.containsVertex(targetVertex);
        return source.degreeOf(sourceVertex) <= target.degreeOf(targetVertex) &&
                targetVertex.getLabels().containsAll(sourceVertex.getLabels());
    }
}
