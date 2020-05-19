package com.charrey.util.datastructures;

import com.charrey.graph.Vertex;
import com.charrey.util.GraphUtil;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FakeGraph implements Graph<Vertex, DefaultEdge> {

    private final Predicate<Vertex> hiddenPredicate;
    private final Graph<Vertex, DefaultEdge> innerGraph;

    public FakeGraph(Graph<Vertex,DefaultEdge> innerGraph, Predicate<Vertex> hidden) {
        this.innerGraph = innerGraph;
        this.hiddenPredicate = hidden;
    }

    @Override
    public Set<DefaultEdge> getAllEdges(Vertex sourceVertex, Vertex targetVertex) {
        if (hiddenPredicate.test(sourceVertex) || hiddenPredicate.test(targetVertex)) {
            return Collections.emptySet();
        } else {
            return innerGraph.getAllEdges(sourceVertex, targetVertex);
        }
    }

    @Override
    public DefaultEdge getEdge(Vertex sourceVertex, Vertex targetVertex) {
        if (hiddenPredicate.test(sourceVertex) || hiddenPredicate.test(targetVertex)) {
            return null;
        } else {
            return innerGraph.getEdge(sourceVertex, targetVertex);
        }
    }

    @Override
    public Supplier<Vertex> getVertexSupplier() {
        return innerGraph.getVertexSupplier();
    }

    @Override
    public Supplier<DefaultEdge> getEdgeSupplier() {
        return innerGraph.getEdgeSupplier();
    }

    @Override
    public DefaultEdge addEdge(Vertex sourceVertex, Vertex targetVertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addEdge(Vertex sourceVertex, Vertex targetVertex, DefaultEdge e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vertex addVertex() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addVertex(Vertex v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsEdge(Vertex sourceVertex, Vertex targetVertex) {
        if (hiddenPredicate.test(sourceVertex) || hiddenPredicate.test(targetVertex)) {
            return false;
        } else {
            return innerGraph.containsEdge(sourceVertex, targetVertex);
        }
    }

    @Override
    public boolean containsEdge(DefaultEdge e) {
        if (hiddenPredicate.test(innerGraph.getEdgeSource(e)) || hiddenPredicate.test(innerGraph.getEdgeTarget(e))) {
            return false;
        } else {
            return innerGraph.containsEdge(e);
        }
    }

    @Override
    public boolean containsVertex(Vertex v) {
        if (hiddenPredicate.test(v)) {
            return false;
        } else {
            return innerGraph.containsVertex(v);
        }
    }

    @Override
    public Set<DefaultEdge> edgeSet() {
        return innerGraph.edgeSet().stream().filter(e -> !hiddenPredicate.test(innerGraph.getEdgeSource(e)) && !hiddenPredicate.test(innerGraph.getEdgeTarget(e))).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int degreeOf(Vertex vertex) {
        return (int) GraphUtil.neighboursOf(innerGraph, vertex).stream().filter(x -> !hiddenPredicate.test(x)).count();
    }

    @Override
    public Set<DefaultEdge> edgesOf(Vertex vertex) {
        if (hiddenPredicate.test(vertex)) {
            throw new IllegalArgumentException("Sst! This vertex is hidden!");
        }
        return innerGraph.edgesOf(vertex).stream().filter(defaultEdge -> {
            Vertex source = innerGraph.getEdgeSource(defaultEdge);
            Vertex target = innerGraph.getEdgeTarget(defaultEdge);
            return (source == vertex || !hiddenPredicate.test(source)) && (target == vertex || !hiddenPredicate.test(target));
        }).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int inDegreeOf(Vertex vertex) {
        return degreeOf(vertex);
    }

    @Override
    public Set<DefaultEdge> incomingEdgesOf(Vertex vertex) {
        return edgesOf(vertex);
    }

    @Override
    public int outDegreeOf(Vertex vertex) {
        return degreeOf(vertex);
    }

    @Override
    public Set<DefaultEdge> outgoingEdgesOf(Vertex vertex) {
        return edgesOf(vertex);
    }

    @Override
    public boolean removeAllEdges(Collection<? extends DefaultEdge> edges) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<DefaultEdge> removeAllEdges(Vertex sourceVertex, Vertex targetVertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllVertices(Collection<? extends Vertex> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DefaultEdge removeEdge(Vertex sourceVertex, Vertex targetVertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEdge(DefaultEdge e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeVertex(Vertex v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Vertex> vertexSet() {
        return innerGraph.vertexSet().stream().filter(x -> !hiddenPredicate.test(x)).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Vertex getEdgeSource(DefaultEdge e) {
        if (hiddenPredicate.test(innerGraph.getEdgeSource(e)) || hiddenPredicate.test(innerGraph.getEdgeTarget(e))) {
            return null;
        } else {
            return innerGraph.getEdgeSource(e);
        }
    }

    @Override
    public Vertex getEdgeTarget(DefaultEdge e) {
        if (hiddenPredicate.test(innerGraph.getEdgeSource(e)) || hiddenPredicate.test(innerGraph.getEdgeTarget(e))) {
            return null;
        } else {
            return innerGraph.getEdgeTarget(e);
        }
    }

    @Override
    public GraphType getType() {
        return innerGraph.getType();
    }

    @Override
    public double getEdgeWeight(DefaultEdge e) {
        if (containsEdge(e)) {
            return innerGraph.getEdgeWeight(e);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setEdgeWeight(DefaultEdge e, double weight) {
        throw new IllegalArgumentException();
    }
}
