package com.charrey.graph.generation;

import com.charrey.graph.Vertex;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.StringWriter;

public class MyGraph extends AbstractBaseGraph<Vertex, DefaultEdge> {
    private final boolean directed;
    private double maxEdgeWeight = 1d;

    public MyGraph(boolean directed) {
        super(
                new GraphGenerator.VertexGenerator(), DefaultEdge::new,
                directed ?
                new DefaultGraphType.Builder()
                        .directed().allowMultipleEdges(false).allowSelfLoops(false).weighted(true)
                        .build() :
                        new DefaultGraphType.Builder()
                                .undirected().allowMultipleEdges(false).allowSelfLoops(false).weighted(true)
                                .build()
                );
        this.directed = directed;
    }

    @Override
    public boolean addEdge(Vertex sourceVertex, Vertex targetVertex, DefaultEdge defaultEdge) {
        boolean res = super.addEdge(sourceVertex, targetVertex, defaultEdge);
        if (res) {
            this.setEdgeWeight(defaultEdge, maxEdgeWeight);
            maxEdgeWeight = Math.nextAfter(maxEdgeWeight, Double.POSITIVE_INFINITY);
        }
        return res;
    }

    @Override
    public DefaultEdge addEdge(Vertex sourceVertex, Vertex targetVertex) {
        DefaultEdge res =  super.addEdge(sourceVertex, targetVertex);
        this.setEdgeWeight(res, maxEdgeWeight);
        maxEdgeWeight = Math.nextAfter(maxEdgeWeight, Double.POSITIVE_INFINITY);
        return res;
    }

    public boolean isDirected() {
        return directed;
    }

    @Override
    public String toString() {
        DOTExporter<Vertex, DefaultEdge> exporter = new DOTExporter<>(x -> Integer.toString(x.data()));
        StringWriter writer = new StringWriter();
        exporter.exportGraph(this, writer);
        return writer.toString();
    }

}
