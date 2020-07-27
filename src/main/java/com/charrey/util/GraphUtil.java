package com.charrey.util;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import gnu.trove.TCollections;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility methods for operations on graphs
 */
public class GraphUtil {

    private GraphUtil() {
    }


    private static final Map<MyGraph, TIntList> cachedRandomVertexOrder = new ConcurrentHashMap<>();
    private static final Map<MyGraph, ConnectivityInspector<Integer, MyEdge>> cachedComponents = new HashMap<>();

    /**
     * Returns the neighbour set of a set of vertices, i.e. each vertex that has some connection to some vertex
     * in the given set.
     *
     * @param graph    the graph
     * @param vertices the set of vertices to look for neighbours of
     * @return all vertices that have some neighbour in vertices.
     */
    @NotNull
    public static TIntSet neighboursOf(@NotNull MyGraph graph, @NotNull Collection<Integer> vertices) {
        TIntSet res = new TIntHashSet();
        vertices.stream().map(x -> Graphs.neighborSetOf(graph, x)).forEach(res::addAll);
        return res;
    }

    /**
     * Deep copies an integer graph with a randomly permuted vertex ordering.
     *
     * @param oldGraph the graph to copy
     * @param random   random generator to randomize the vertex ordering
     * @return a copy of the graph with a new random vertex ordering
     */
    @NotNull
    public static MyGraph copy(@NotNull MyGraph oldGraph, RandomGenerator random) {
        MyGraph newGraph = new MyGraph(oldGraph.isDirected());
        int[] oldVertexToNew = new int[oldGraph.vertexSet().size()];
        List<Integer> oldVertices = new LinkedList<>(oldGraph.vertexSet());
        oldVertices.sort(Integer::compareTo);
        Collections.shuffle(oldVertices, new RandomAdaptor(random));
        for (int oldVertex : oldVertices) {
            oldVertexToNew[oldVertex] = newGraph.addVertex();
            oldGraph.getAttributes(oldVertex).forEach((key, value1) -> value1.forEach(value -> newGraph.addAttribute(oldVertexToNew[oldVertex], key, value)));
        }

        List<MyEdge> oldEdges = new LinkedList<>(oldGraph.edgeSet());
        oldEdges.sort((o1, o2) -> {
            int compareFirst = Integer.compare(oldGraph.getEdgeSource(o1), oldGraph.getEdgeSource(o2));
            int compareSecond = Integer.compare(oldGraph.getEdgeTarget(o1), oldGraph.getEdgeTarget(o2));
            return compareFirst == 0 ? compareSecond : compareFirst;
        });
        Collections.shuffle(oldEdges, new RandomAdaptor(random));
        for (MyEdge oldEdge : oldEdges) {
            newGraph.addEdge(oldVertexToNew[oldGraph.getEdgeSource(oldEdge)], oldVertexToNew[oldGraph.getEdgeTarget(oldEdge)]);
        }
        return newGraph;
    }

    /**
     * Returns all vertices reachable from a specific source in the graph (ignoring labels)
     *
     * @param graph  the graph that contains the vertices
     * @param source the vertex to search from
     * @return the set of all reachable vertices
     */
    public static synchronized TIntSet reachableNeighbours(@NotNull MyGraph graph, int source) { //todo: only arcs, wires and SLICE
        cachedComponents.putIfAbsent(graph, new ConnectivityInspector<>(graph));
        TIntSet toReturn = new TIntHashSet();
        Set<Integer> connected = cachedComponents.get(graph).connectedSetOf(source);
        for (Integer connectedInt : connected) {
            if (connectedInt != source) {
                toReturn.add(connectedInt);
            }
        }
        return TCollections.unmodifiableSet(toReturn);
    }

    /**
     * Returns the vertices of a graph in a fixed random order. (quickly, since this is cached).
     *
     * @param graph  the graph to query the vertices of
     * @param random the random
     * @return vertices of the graph in deterministically random order.
     */
    @NotNull
    public static TIntList randomVertexOrder(@NotNull MyGraph graph, @NotNull Random random) {
        if (!cachedRandomVertexOrder.containsKey(graph)) {
            synchronized (graph) {
                if (!cachedRandomVertexOrder.containsKey(graph)) {
                    TIntList toAdd = new TIntArrayList();
                    graph.vertexSet().forEach(toAdd::add);
                    toAdd.shuffle(random);
                    toAdd = TCollections.unmodifiableList(toAdd);
                    cachedRandomVertexOrder.put(graph, toAdd);
                    return toAdd;
                }
            }
        }
        return cachedRandomVertexOrder.get(graph);
    }

    public static MyGraph repairVertices(MyGraph targetGraph) {
        int[] permutation = new int[targetGraph.vertexSet().size()];
        int[] reversePermutation = new int[targetGraph.vertexSet().stream().mapToInt(x -> x).max().getAsInt() + 1];
        int counter = 0;
        List<Integer> vertexList = new ArrayList<>(targetGraph.vertexSet());
        for (Integer integer : vertexList) {
            permutation[counter] = integer;
            reversePermutation[integer] = counter;
            counter++;
        }
        return MyGraph.applyOrdering(targetGraph, permutation, reversePermutation);
    }

    public static TIntSet radiusNeighbourHood(MyGraph sourceGraphVertex, int targetGraphVertex, int radius) {

        throw new UnsupportedOperationException();
    }
}
