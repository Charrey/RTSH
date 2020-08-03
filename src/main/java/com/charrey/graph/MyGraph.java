package com.charrey.graph;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.util.SupplierUtil;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A graph class that uses integers for vertices and supports multiple labels for each vertex. Self-loops and multiple
 * edges between the same pairs are disallowed.
 */
public class MyGraph extends AbstractBaseGraph<Integer, MyEdge> {
    private static final String GRAPH_IS_LOCKED_MESSAGE = "Graph is locked!";
    private static final String LABEL = "label";


    private final boolean directed;
    private double maxEdgeWeight = 1d;
    private final List<Map<String, Set<String>>> attributes;
    private boolean locked = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MyGraph myGraph = (MyGraph) o;
        return directed == myGraph.directed &&
                attributes.equals(myGraph.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), directed, attributes);
    }

    /**
     * Instantiates a new empty graph.
     *
     * @param directed whether the graph is directed. If false, the graph will be undirected.
     */
    public MyGraph(boolean directed) {
        super(
                SupplierUtil.createIntegerSupplier(), new MyEdge.MyEdgeSupplier(),
                directed ?
                        new DefaultGraphType.Builder()
                                .directed().allowMultipleEdges(false).allowSelfLoops(false).weighted(true)
                                .build() :
                        new DefaultGraphType.Builder()
                                .undirected().allowMultipleEdges(false).allowSelfLoops(false).weighted(true)
                                .build()
        );
        this.directed = directed;
        attributes = new ArrayList<>();
    }

    /**
     * Applies a new vertex ordering to a graph, yielding a new graph that has this ordering. The old graph remains
     * unmodified.
     *
     * @param source     the graph to which to apply the new vertex ordering.
     * @param newToOld the new ordering, such that the position of integers is the new vertex value, and the value of                   the integers is the old vertex value.
     * @return a graph such that the ordering is applied.
     */
    public static MyGraph applyOrdering(MyGraph source, int[] newToOld, int[] oldToNew) {
        MyGraph res = new MyGraph(source.directed);
        for (int newVertex = 0; newVertex < source.vertexSet().size(); newVertex++) {
            int newVertexFinal = newVertex;
            res.addVertex(newVertex);
            int oldVertex = newToOld[newVertex];
            source.attributes.get(oldVertex).forEach((key, values) -> values.forEach(value -> res.addAttribute(newVertexFinal, key, value)));
            Set<Integer> predecessors = Graphs.predecessorListOf(source, oldVertex).stream().map(x -> oldToNew[x]).filter(x -> x < newVertexFinal).collect(Collectors.toUnmodifiableSet());
            predecessors.forEach(x -> res.addEdge(x, newVertexFinal));
            Set<Integer> successors = new HashSet<>(Graphs.successorListOf(source, oldVertex).stream().map(x -> oldToNew[x]).filter(x -> x < newVertexFinal).collect(Collectors.toUnmodifiableSet()));
            if (!source.directed) {
                successors.removeAll(predecessors);
            }
            successors.forEach(x -> res.addEdge(newVertexFinal, x));
        }
        return res;
    }

    @Override
    public Integer addVertex() {
        if (locked) {
            throw new IllegalStateException(GRAPH_IS_LOCKED_MESSAGE);
        }
        int toReturn = super.addVertex();
        attributes.add(new HashMap<>());
        assert toReturn == attributes.size() - 1;
        return toReturn;
    }

    public int addVertex(String... attributes) {
        if (attributes.length % 2 != 0) {
            throw new IllegalArgumentException("The number of string parameters must be even: a set of key-value pairs!");
        }
        Integer added = addVertex();
        for (int i = 0; i < attributes.length; i += 2) {
            addAttribute(added, attributes[i], attributes[i + 1]);
        }
        return added;
    }

    @Override
    public boolean addEdge(Integer sourceVertex, Integer targetVertex, MyEdge defaultEdge) {
        if (locked) {
            throw new IllegalStateException(GRAPH_IS_LOCKED_MESSAGE);
        }
        defaultEdge.setSource(sourceVertex);
        defaultEdge.setTarget(targetVertex);
        return super.addEdge(sourceVertex, targetVertex, defaultEdge);
    }

    public void randomizeWeights() {
        if (locked) {
            throw new IllegalStateException(GRAPH_IS_LOCKED_MESSAGE);
        }
        int edgeSetSize = edgeSet().size();
        if (edgeSetSize <= 1) {
            return;
        }
        double maxWeightDiff = 1d / ((edgeSetSize - 1) / 2d);
        Random random = new Random(710);
        for (MyEdge edge : edgeSet()) {
            double weight = 1 + (random.nextDouble() * maxWeightDiff) - (0.5 * maxWeightDiff);
            setEdgeWeight(edge, weight);
        }
    }

    @Override
    public MyEdge addEdge(Integer sourceVertex, Integer targetVertex) {
        if (locked) {
            throw new IllegalStateException(GRAPH_IS_LOCKED_MESSAGE);
        }
        if (containsEdge(sourceVertex, targetVertex)) {
            throw new IllegalArgumentException("This edge already exists!");
        }
        MyEdge res = new MyEdge(sourceVertex, targetVertex);
        super.addEdge(sourceVertex, targetVertex, res);
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

    public void lock() {
        this.locked = true;
    }

    @Override
    public String toString() {
        DOTExporter<Integer, MyEdge> exporter = new DOTExporter<>(x -> Integer.toString(x));
        exporter.setVertexAttributeProvider(integer ->
                attributes.get(integer).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        x -> new DefaultAttribute<>((x.getKey().equals(LABEL) ? integer + " " : "") + x.getValue().toString(), AttributeType.STRING))));
        StringWriter writer = new StringWriter();
        exporter.exportGraph(this, writer);
        return writer.toString();
    }

    @Override
    public boolean addVertex(Integer vertex) {
        if (locked) {
            throw new IllegalStateException(GRAPH_IS_LOCKED_MESSAGE);
        }
        boolean toReturn = super.addVertex(vertex);
        if (toReturn) {
            attributes.add(new HashMap<>());
            if (vertex != attributes.size() - 1) {
                throw new IllegalStateException("Vertices must be added in ascending consecutive order!");
            }
        }
        return toReturn;
    }

    /**
     * Returns all attributes of a vertex. The data at the index of the vertex in the provided
     * list is a map that provides a set of values for each key.
     *
     * @param vertex the vertex to request attributes of
     * @return all attributes of that vertex
     */
    public Map<String, Set<String>> getAttributes(int vertex) {
        return Collections.unmodifiableMap(attributes.get(vertex));
    }

    /**
     * Returns all labels of a specific vertex
     *
     * @param vertex vertex to retrieve the labels of
     * @return set of labels of this vertex
     * @throws IllegalArgumentException thrown if the graph did not contain the provided vertex.
     */
    @NotNull
    public Collection<String> getLabels(int vertex) {
        if (!containsVertex(vertex)) {
            throw new IllegalArgumentException("The graph must contain the vertex " + vertex);
        }
        attributes.get(vertex).computeIfAbsent(LABEL, x -> new HashSet<>());
        return attributes.get(vertex).get(LABEL);
    }

    /**
     * Stores a key-value pair of an attribute of a specific vertex. If the key is "label" it is interpreted as a label.
     *
     * @param vertex the vertex whose attribute is being added
     * @param key    the attribute key
     * @param value  the attribute value
     * @throws IllegalArgumentException thrown if the graph did not contain the provided vertex.
     */
    public void addAttribute(Integer vertex, String key, String value) {
        if (!containsVertex(vertex)) {
            throw new IllegalArgumentException("The graph must contain the vertex " + vertex);
        }
        attributes.get(vertex).computeIfAbsent(key, x -> new HashSet<>());
        attributes.get(vertex).get(key).add(value);
    }


}