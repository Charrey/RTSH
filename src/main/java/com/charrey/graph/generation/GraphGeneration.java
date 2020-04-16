package com.charrey.graph.generation;

import com.charrey.graph.RoutingVertexTable;
import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.Serializable;
import java.io.StringWriter;

public class GraphGeneration implements Serializable {
    private final Graph<Vertex, DefaultEdge> graph;
    //private final RoutingVertexTable routingTable;

    public GraphGeneration(Graph<Vertex, DefaultEdge> graph, RoutingVertexTable routingTable) {
        this.graph = graph;
        //this.routingTable = routingTable;
    }

    public Graph<Vertex, DefaultEdge> getGraph() {
        return graph;
    }

   // public RoutingVertexTable getRoutingTable() {
  //      return routingTable;
  //  }

    @Override
    public String toString() {
        DOTExporter<Vertex, DefaultEdge> exporter = new DOTExporter<>(x -> Integer.toString(x.intData()));
        StringWriter writer = new StringWriter();
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }
}
