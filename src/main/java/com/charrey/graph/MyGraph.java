package com.charrey.graph;

import org.jgrapht.Graphs;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.util.SupplierUtil;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A graph class that uses integers for vertices and supports multiple labels for each vertex. Self-loops and multiple
 * edges between the same pairs are disallowed.
 */
public class MyGraph extends AbstractBaseGraph<Integer, DefaultEdge> {
    private final boolean directed;
    private double maxEdgeWeight = 1d;


    private final List<Map<String, Set<String>>> labels;

    /**
     * Instantiates a new empty graph.
     *
     * @param directed whether the graph is directed. If false, the graph will be undirected.
     */
    public MyGraph(boolean directed) {
        super(
                SupplierUtil.createIntegerSupplier(), DefaultEdge::new,
                directed ?
                new DefaultGraphType.Builder()
                        .directed().allowMultipleEdges(false).allowSelfLoops(false).weighted(true)
                        .build() :
                        new DefaultGraphType.Builder()
                                .undirected().allowMultipleEdges(false).allowSelfLoops(false).weighted(true)
                                .build()
                );
        this.directed = directed;
        labels = new ArrayList<>();
    }

    @Override
    public Integer addVertex() {
        int toReturn = super.addVertex();
        labels.add(new HashMap<>());
        assert toReturn == labels.size() - 1;
        return toReturn;
    }

    @Override
    public boolean addVertex(Integer vertex) {
        boolean toReturn = super.addVertex(vertex);
        if (toReturn) {
            labels.add(new HashMap<>());
            assert vertex == labels.size() - 1;
        }
        return toReturn;
    }

    @Override
    public boolean addEdge(Integer sourceVertex, Integer targetVertex, DefaultEdge defaultEdge) {
        boolean res = super.addEdge(sourceVertex, targetVertex, defaultEdge);
        if (res) {
            this.setEdgeWeight(defaultEdge, maxEdgeWeight);
            maxEdgeWeight = Math.nextAfter(maxEdgeWeight, Double.POSITIVE_INFINITY);
        }
        return res;
    }

    @Override
    public DefaultEdge addEdge(Integer sourceVertex, Integer targetVertex) {
        assert  !containsEdge(sourceVertex, targetVertex);
        assert sourceVertex!=null;
        assert targetVertex!=null;
        DefaultEdge res =  super.addEdge(sourceVertex, targetVertex);
        assert res != null;
        this.setEdgeWeight(res, maxEdgeWeight);
        maxEdgeWeight = Math.nextAfter(maxEdgeWeight, Double.POSITIVE_INFINITY);
        return res;
    }

    /**
     * Returns whether this graph is directed.
     *
     * @return true if this graph is directed or false if it is undirected.
     */
    public boolean isDirected() {
        return directed;
    }

    @Override
    public String toString() {
        DOTExporter<Integer, DefaultEdge> exporter = new DOTExporter<>(x -> Integer.toString(x));
        StringWriter writer = new StringWriter();
        exporter.exportGraph(this, writer);
        return writer.toString();
    }

    /**
     * Applies a new vertex ordering to a graph, yielding a new graph that has this ordering. The old graph remains
     * unmodified.
     *
     * @param source     the graph to which to apply the new vertex ordering.
     * @param new_to_old the new ordering, such that the position of integers is the new vertex value, and the value of                   the integers is the old vertex value.
     * @return a graph such that the ordering is applied.
     */
    public static MyGraph applyOrdering(MyGraph source, int[] new_to_old) {
        int[] old_to_new = new int[new_to_old.length];
        for (int i = 0; i < new_to_old.length; i++) {
            old_to_new[new_to_old[i]] = i;
        }
        MyGraph res = new MyGraph(source.directed);
        for (int new_vertex = 0; new_vertex < source.vertexSet().size(); new_vertex++) {
            int i_final = new_vertex;
            res.addVertex(new_vertex);
            int old_vertex = new_to_old[new_vertex];
            source.labels.get(new_vertex).forEach((key, values) -> values.forEach(value -> res.addAttribute(i_final, key, value)));
            Set<Integer> predecessors = Graphs.predecessorListOf(source, old_vertex).stream().map(x -> old_to_new[x]).filter(x -> x < i_final).collect(Collectors.toUnmodifiableSet());
            predecessors.forEach(x -> res.addEdge(x, i_final));
            Set<Integer> successors = new HashSet<>(Graphs.successorListOf(source, old_vertex).stream().map(x -> old_to_new[x]).filter(x -> x < i_final).collect(Collectors.toUnmodifiableSet()));
            if (!source.directed) {
                successors.removeAll(predecessors);
            }
            successors.forEach(x -> res.addEdge(i_final, x));
        }
        return res;
    }

    /**
     * Returns all labels of a specific vertex
     *
     * @param vertex vertex to retrieve the labels of
     * @return set of labels of this vertex
     * @throws IllegalArgumentException thrown if the graph did not contain the provided vertex.
     */
    public Collection<String> getLabels(int vertex) {
        if (!containsVertex(vertex)) {
            throw new IllegalArgumentException("The graph must contain the vertex " + vertex);
        }
        labels.get(vertex).computeIfAbsent("label", x -> new HashSet<>());
        return labels.get(vertex).get("label");
    }

    /**
     * Stores a key-value pair of an attribute of a specific vertex. If the key is "label" it is interpreted as a label.
     *
     * @param vertex the vertex whose attribute is being added
     * @param key    the attribute key
     * @param value  the attribute value
     * @throws IllegalArgumentException thrown if the graph did not contain the provided vertex.
     */
    public void addAttribute(int vertex, String key, String value) {
        if (!containsVertex(vertex)) {
            throw new IllegalArgumentException("The graph must contain the vertex " + vertex);
        }
        labels.get(vertex).computeIfAbsent(key, x -> new HashSet<>());
        labels.get(vertex).get(key).add(value);
    }
}