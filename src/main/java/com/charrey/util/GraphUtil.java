package com.charrey.util;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.settings.Settings;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class GraphUtil {


    public static  Set<Vertex> neighboursOf(MyGraph g, Vertex vertex) {
        return g.edgesOf(vertex)
                .stream()
                .map(o -> g.getEdgeSource(o) == vertex ? g.getEdgeTarget(o) : g.getEdgeSource(o))
                .collect(Collectors.toSet());
    }

    public static  Set<Vertex> neighboursOf(MyGraph g, Collection<Vertex> vertices) {
        Set<Vertex> res = new HashSet<>();
        vertices.stream().map(x -> neighboursOf(g, x)).forEach(res::addAll);
        return res;
    }

    public static MyGraph copy(MyGraph pattern, RandomGenerator random) {
        MyGraph res = new MyGraph(pattern.isDirected());
        Vertex[] mapping = new Vertex[pattern.vertexSet().size()];

        List<Vertex> vertices = new LinkedList<>(pattern.vertexSet());
        vertices.sort(Comparator.comparingInt(Vertex::data));
        Collections.shuffle(vertices, new RandomAdaptor(random));
        for (Vertex v : vertices) {
            Vertex added = res.addVertex();
            mapping[v.data()] = added;
        }
        List<DefaultEdge> edges = new LinkedList<>(pattern.edgeSet());
        edges.sort((o1, o2) -> {
            int compareFirst = Integer.compare(pattern.getEdgeSource(o1).data(), pattern.getEdgeSource(o2).data());
            int compareSecond = Integer.compare(pattern.getEdgeTarget(o1).data(), pattern.getEdgeTarget(o2).data());
            return compareFirst == 0 ? compareSecond : compareFirst;
        });
        Collections.shuffle(edges, new RandomAdaptor(random));
        for (DefaultEdge e : edges) {
            res.addEdge(mapping[pattern.getEdgeSource(e).data()], mapping[pattern.getEdgeTarget(e).data()]);
        }
        return res;
    }

    private static final Map<MyGraph, ConnectivityInspector<Vertex, DefaultEdge>> cachedComponents = new HashMap<>();
    public static Set<Vertex> reachableNeighbours(MyGraph graph, Vertex source) {
        cachedComponents.putIfAbsent(graph, new ConnectivityInspector<>(graph));
        return cachedComponents.get(graph).connectedSetOf(source).stream().filter(x -> x != source).collect(Collectors.toUnmodifiableSet());
    }

    private static MyGraph graph;
    private static List<Vertex> cachedRandomVertexOrder = null;
    public static List<Vertex> randomVertexOrder(MyGraph graph) {
        if (!graph.equals(GraphUtil.graph) || cachedRandomVertexOrder == null) {
            cachedRandomVertexOrder = new ArrayList<>(graph.vertexSet());
            Collections.shuffle(cachedRandomVertexOrder, Settings.instance.random);
            cachedRandomVertexOrder = Collections.unmodifiableList(cachedRandomVertexOrder);
            GraphUtil.graph = graph;
        }
        return cachedRandomVertexOrder;
    }
}
