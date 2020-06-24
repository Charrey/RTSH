package com.charrey.util;

import com.charrey.graph.MyGraph;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class GraphUtil {


    public static  Set<Integer> neighboursOf(@NotNull MyGraph g, int vertex) {
        return g.edgesOf(vertex)
                .stream()
                .map(o -> g.getEdgeSource(o) == vertex ? g.getEdgeTarget(o) : g.getEdgeSource(o))
                .collect(Collectors.toSet());
    }

    @NotNull
    public static  Set<Integer> neighboursOf(@NotNull MyGraph g, @NotNull Collection<Integer> vertices) {
        Set<Integer> res = new HashSet<>();
        vertices.stream().map(x -> neighboursOf(g, x)).forEach(res::addAll);
        return res;
    }

    @NotNull
    public static MyGraph copy(@NotNull MyGraph pattern, RandomGenerator random) {
        MyGraph res = new MyGraph(pattern.isDirected());
        int[] mapping = new int[pattern.vertexSet().size()];

        List<Integer> vertices = new LinkedList<>(pattern.vertexSet());
        vertices.sort(Integer::compareTo);
        Collections.shuffle(vertices, new RandomAdaptor(random));
        for (int v : vertices) {
            int added = res.addVertex();
            mapping[v] = added;
        }
        List<DefaultEdge> edges = new LinkedList<>(pattern.edgeSet());
        edges.sort((o1, o2) -> {
            int compareFirst = Integer.compare(pattern.getEdgeSource(o1), pattern.getEdgeSource(o2));
            int compareSecond = Integer.compare(pattern.getEdgeTarget(o1), pattern.getEdgeTarget(o2));
            return compareFirst == 0 ? compareSecond : compareFirst;
        });
        Collections.shuffle(edges, new RandomAdaptor(random));
        for (DefaultEdge e : edges) {
            res.addEdge(mapping[pattern.getEdgeSource(e)], mapping[pattern.getEdgeTarget(e)]);
        }
        return res;
    }

    private static final Map<MyGraph, ConnectivityInspector<Integer, DefaultEdge>> cachedComponents = new HashMap<>();
    public static Set<Integer> reachableNeighbours(@NotNull MyGraph graph, int source) {
        cachedComponents.putIfAbsent(graph, new ConnectivityInspector<>(graph));
        return cachedComponents.get(graph).connectedSetOf(source).stream().filter(x -> x != source).collect(Collectors.toUnmodifiableSet());
    }

    private static MyGraph graph;
    @Nullable
    private static List<Integer> cachedRandomVertexOrder = null;
    @NotNull
    public static List<Integer> randomVertexOrder(@NotNull MyGraph graph, @NotNull Random random) {
        if (!graph.equals(GraphUtil.graph) || cachedRandomVertexOrder == null) {
            cachedRandomVertexOrder = new ArrayList<>(graph.vertexSet());
            Collections.shuffle(cachedRandomVertexOrder, random);
            cachedRandomVertexOrder = Collections.unmodifiableList(cachedRandomVertexOrder);
            GraphUtil.graph = graph;
        }
        return Collections.unmodifiableList(cachedRandomVertexOrder);
    }
}
