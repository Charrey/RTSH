package com.charrey.util;

import com.charrey.algorithms.FixedPoint;
import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.Attribute;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GraphUtil {


    public static VertexColoringAlgorithm.Coloring<Vertex> getLabelColoring(Graph<Vertex, DefaultEdge> pattern) {
        Map<Set<Attribute>, Set<Vertex>> labelSets = new HashMap<>();
        for (Vertex v : pattern.vertexSet()) {
            labelSets.putIfAbsent(v.getLabels(), new HashSet<>());
            labelSets.get(v.getLabels()).add(v);
        }
        List<Set<Vertex>> asList = new ArrayList<>(labelSets.values());
        Map<Vertex, Integer> colouring = new HashMap<>();
        for (int colourClass = 0; colourClass < asList.size(); colourClass++) {
            for (Vertex v : asList.get(colourClass)) {
                colouring.put(v, colourClass);
            }
        }


        return new VertexColoringAlgorithm.Coloring<>() {
            @Override
            public int getNumberColors() {
                return labelSets.size();
            }

            @Override
            public Map<Vertex, Integer> getColors() {
                return colouring;
            }

            @Override
            public List<Set<Vertex>> getColorClasses() {
                return asList;
            }
        };
    }

    public static  Set<Vertex> neighboursOf(Graph<Vertex, DefaultEdge> g, Vertex vertex) {
        return g.edgesOf(vertex)
                .stream()
                .map(o -> g.getEdgeSource(o) == vertex ? g.getEdgeTarget(o) : g.getEdgeSource(o))
                .collect(Collectors.toSet());
    }

    public static  Set<Vertex> neighboursOf(Graph<Vertex, DefaultEdge> g, Collection<Vertex> vertices) {
        Set<Vertex> res = new HashSet<>();
        vertices.stream().map(x -> neighboursOf(g, x)).forEach(res::addAll);
        return res;
    }

    public static Set<Vertex> floodNeighboursOf(Graph<Vertex, DefaultEdge> graph, Vertex source, Predicate<Vertex> floodable) {
        Set<Vertex> floodedRouting = FixedPoint.explore(source, x -> neighboursOf(graph, x).stream().filter(floodable).collect(Collectors.toSet()));
        floodedRouting.addAll(neighboursOf(graph, floodedRouting));
        floodedRouting.remove(source);
        return floodedRouting;
    }

    static Map<Graph<Vertex, DefaultEdge>, Integer[]> edgesMatchedCache = new HashMap<>();
    public static Integer[] edgesMatched(Graph<Vertex, DefaultEdge> graph) {
        if (edgesMatchedCache.containsKey(graph)) {
            return edgesMatchedCache.get(graph);
        }
        List<Vertex> vertices = new ArrayList<>(graph.vertexSet());
        Collections.sort(vertices);
        Set<Vertex> seen = new HashSet<>();
        Integer[] res = new Integer[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            int newEdges = Math.toIntExact(GraphUtil.neighboursOf(graph, vertices.get(i)).stream().filter(seen::contains).count());
            res[i] = newEdges + (i > 0 ? res[i-1] : 0);
            seen.add(vertices.get(i));
        }
        edgesMatchedCache.put(graph, res);
        return res;
    }

    public static Graph<Vertex, DefaultEdge> copy(Graph<Vertex, DefaultEdge> pattern, Random random) {
        AnyGenerator<Integer> numbers = new AnyGenerator<>(0, x -> ++x);
        Graph<Vertex, DefaultEdge> res = new SimpleGraph<>(() -> new Vertex(numbers.get()), DefaultEdge::new, false);
        Map<Vertex, Vertex> mapping = new HashMap<>();

        List<Vertex> vertices = new LinkedList<>(pattern.vertexSet());
        vertices.sort(Comparator.comparingInt(Vertex::intData));
        Collections.shuffle(vertices, random);
        for (Vertex v : vertices) {
            mapping.put(v, res.addVertex());
        }
        List<DefaultEdge> edges = new LinkedList<>(pattern.edgeSet());
        edges.sort((o1, o2) -> {
            int compareFirst = Integer.compare(pattern.getEdgeSource(o1).intData(), pattern.getEdgeSource(o2).intData());
            int compareSecond = Integer.compare(pattern.getEdgeTarget(o1).intData(), pattern.getEdgeTarget(o2).intData());
            return compareFirst == 0 ? compareSecond : compareFirst;
        });
        Collections.shuffle(edges);
        for (DefaultEdge e : edges) {
            res.addEdge(mapping.get(pattern.getEdgeSource(e)), mapping.get(pattern.getEdgeTarget(e)));
        }
        return res;
    }

    public static void fixIntData(Graph<Vertex, DefaultEdge> graph) {
        Iterator<Vertex> vertexIterator = graph.vertexSet().iterator();
        int data = 0;
        while (vertexIterator.hasNext()) {
            Vertex nextVertex = vertexIterator.next();
            nextVertex.setData(data++);
            assert data > 0;
        }
    }

    private static Map<Graph<Vertex, DefaultEdge>, ConnectivityInspector<Vertex, DefaultEdge>> cachedComponents = new HashMap<>();
    public static Set<Vertex> reachableNeighbours(Graph<Vertex, DefaultEdge> graph, Vertex source) {
        cachedComponents.putIfAbsent(graph, new ConnectivityInspector<>(graph));
        return cachedComponents.get(graph).connectedSetOf(source).stream().filter(x -> x != source).collect(Collectors.toUnmodifiableSet());
    }
}
