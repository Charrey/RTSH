package com.charrey.util;

import com.charrey.algorithms.FixedPoint;
import com.charrey.graph.AttributedVertex;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GraphUtil {


    public static <V, E> VertexColoringAlgorithm.Coloring<V> getLabelColoring(Graph<V, E> pattern) {
        Map<Set<Attribute>, Set<V>> labelSets = new HashMap<>();
        for (V v : pattern.vertexSet()) {
            labelSets.putIfAbsent(((AttributedVertex<Comparable>)v).getLabels(), new HashSet<>());
            labelSets.get(((AttributedVertex<Comparable>)v).getLabels()).add(v);
        }
        List<Set<V>> asList = new ArrayList<>(labelSets.values());
        Map<V, Integer> colouring = new HashMap<>();
        for (int colourClass = 0; colourClass < asList.size(); colourClass++) {
            for (V v : asList.get(colourClass)) {
                colouring.put(v, colourClass);
            }
        }


        return new VertexColoringAlgorithm.Coloring<V>() {
            @Override
            public int getNumberColors() {
                return labelSets.size();
            }

            @Override
            public Map<V, Integer> getColors() {
                return colouring;
            }

            @Override
            public List<Set<V>> getColorClasses() {
                return asList;
            }
        };
    }

    public static <V, E> Set<V> neighboursOf(Graph<V, E> g, V vertex) {
        return g.edgesOf(vertex)
                .stream()
                .map(o -> g.getEdgeSource(o) == vertex ? g.getEdgeTarget(o) : g.getEdgeSource(o))
                .collect(Collectors.toSet());
    }

    public static <V, E> Set<V> neighboursOf(Graph<V, E> g, Collection<V> vertices) {
        Set<V> res = new HashSet<>();
        vertices.stream().map(x -> neighboursOf(g, x)).forEach(res::addAll);
        return res;
    }

    public static <V, E> Set<V> floodNeighboursOf(Graph<V, E> graph, V source, Predicate<V> floodable) {
        Set<V> floodedRouting = FixedPoint.explore(source, x -> neighboursOf(graph, x).stream().filter(floodable).collect(Collectors.toSet()));
        floodedRouting.addAll(neighboursOf(graph, floodedRouting));
        floodedRouting.remove(source);
        return floodedRouting;
    }

    static Map<Graph, Integer[]> edgesMatchedCache = new HashMap<>();
    public static  <V extends Comparable<V>, E> Integer[] edgesMatched(Graph<V, E> graph) {
        if (edgesMatchedCache.containsKey(graph)) {
            return edgesMatchedCache.get(graph);
        }
        List<V> vertices = null;
        vertices = new ArrayList<>(graph.vertexSet());
        Collections.sort(vertices);
        Set<V> seen = new HashSet<>();
        Integer[] res = new Integer[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            int newEdges = Math.toIntExact(GraphUtil.neighboursOf(graph, vertices.get(i)).stream().filter(seen::contains).count());
            res[i] = newEdges + (i > 0 ? res[i-1] : 0);
            seen.add(vertices.get(i));
        }
        edgesMatchedCache.put(graph, res);
        return res;
    }

    public static  <V1, V2> Map<V2, V2>[] getToTryNext(List<V1> order, Map<V1, Set<V2>> compatibility, Graph<V2, DefaultEdge> targetGraph) {
        Map<V2, V2>[] toTryNext = new HashMap[order.size()];
        for (int i = 0; i < order.size(); i++) {
            toTryNext[i] = new HashMap<>();
            V2 previous = null;
            for (V2 targetVertex : compatibility.get(order.get(i))) {
                toTryNext[i].put(previous, targetVertex);
                previous = targetVertex;
            }
            toTryNext[i].put(previous, null);
        }
        return toTryNext;
    }
}
