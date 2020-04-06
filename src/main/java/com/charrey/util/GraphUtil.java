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


    public static VertexColoringAlgorithm.Coloring<AttributedVertex> getLabelColoring(Graph<AttributedVertex, DefaultEdge> pattern) {
        Map<Set<Attribute>, Set<AttributedVertex>> labelSets = new HashMap<>();
        for (AttributedVertex v : pattern.vertexSet()) {
            labelSets.putIfAbsent(v.getLabels(), new HashSet<>());
            labelSets.get(v.getLabels()).add(v);
        }
        List<Set<AttributedVertex>> asList = new ArrayList<>(labelSets.values());
        Map<AttributedVertex, Integer> colouring = new HashMap<>();
        for (int colourClass = 0; colourClass < asList.size(); colourClass++) {
            for (AttributedVertex v : asList.get(colourClass)) {
                colouring.put(v, colourClass);
            }
        }


        return new VertexColoringAlgorithm.Coloring<>() {
            @Override
            public int getNumberColors() {
                return labelSets.size();
            }

            @Override
            public Map<AttributedVertex, Integer> getColors() {
                return colouring;
            }

            @Override
            public List<Set<AttributedVertex>> getColorClasses() {
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

    static Map<Graph<AttributedVertex, DefaultEdge>, Integer[]> edgesMatchedCache = new HashMap<>();
    public static  Integer[] edgesMatched(Graph<AttributedVertex, DefaultEdge> graph) {
        if (edgesMatchedCache.containsKey(graph)) {
            return edgesMatchedCache.get(graph);
        }
        List<AttributedVertex> vertices = new ArrayList<>(graph.vertexSet());
        Collections.sort(vertices);
        Set<AttributedVertex> seen = new HashSet<>();
        Integer[] res = new Integer[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            int newEdges = Math.toIntExact(GraphUtil.neighboursOf(graph, vertices.get(i)).stream().filter(seen::contains).count());
            res[i] = newEdges + (i > 0 ? res[i-1] : 0);
            seen.add(vertices.get(i));
        }
        edgesMatchedCache.put(graph, res);
        return res;
    }

    public static  Map<AttributedVertex, AttributedVertex>[] getToTryNext(List<AttributedVertex> order, Map<AttributedVertex, Set<AttributedVertex>> compatibility, Graph<AttributedVertex, DefaultEdge> targetGraph) {
        @SuppressWarnings("unchecked") Map<AttributedVertex, AttributedVertex>[] toTryNext = new HashMap[order.size()];
        for (int i = 0; i < order.size(); i++) {
            toTryNext[i] = new HashMap<>();
            AttributedVertex previous = null;
            for (AttributedVertex targetVertex : compatibility.get(order.get(i))) {
                toTryNext[i].put(previous, targetVertex);
                previous = targetVertex;
            }
            toTryNext[i].put(previous, null);
        }
        return toTryNext;
    }
}
