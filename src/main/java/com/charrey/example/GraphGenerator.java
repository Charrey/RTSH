package com.charrey.example;

import com.charrey.graph.RoutingVertexTable;
import com.charrey.util.AnyGenerator;
import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.StringWriter;

public class GraphGenerator {


    public static GraphGeneration getTarget() {
        AnyGenerator<Integer> gen = new AnyGenerator<>(0, x -> ++x);
        Graph<Vertex, DefaultEdge> pattern = new SimpleGraph<>(() -> new Vertex(gen.get()), DefaultEdge::new, false);
        Vertex p1 = pattern.addVertex().addLabel("normal");
        Vertex p2 = pattern.addVertex().addLabel("normal");
        Vertex p3 = pattern.addVertex().addLabel("normal");
        Vertex p4 = pattern.addVertex().addLabel("normal");
        pattern.addEdge(p1, p2);
        pattern.addEdge(p2, p4);
        pattern.addEdge(p4, p1);
        pattern.addEdge(p3, p2);
        return new GraphGeneration(pattern, new RoutingVertexTable());
    }


    public static GraphGeneration getPattern() {
        AnyGenerator<Integer> gen = new AnyGenerator<>(0, x -> ++x);
        Graph<Vertex, DefaultEdge> pattern = new SimpleGraph<>(() -> new Vertex(gen.get()), DefaultEdge::new, false);
        Vertex p2 = pattern.addVertex().addLabel("normal");
        Vertex p3 = pattern.addVertex().addLabel("normal");
        Vertex p4 = pattern.addVertex().addLabel("normal");
        pattern.addEdge(p2, p3);
        pattern.addEdge(p2, p4);
        pattern.addEdge(p3, p4);
        return new GraphGeneration(pattern, new RoutingVertexTable());
    }

    public static class GraphGeneration {
        private final Graph<Vertex, DefaultEdge> graph;
        private final RoutingVertexTable routingTable;

        public GraphGeneration(Graph<Vertex, DefaultEdge> graph, RoutingVertexTable routingTable) {
            this.graph = graph;
            this.routingTable = routingTable;
        }

        public Graph<Vertex, DefaultEdge> getGraph() {
            return graph;
        }

        public RoutingVertexTable getRoutingTable() {
            return routingTable;
        }

        @Override
        public String toString() {
            DOTExporter<Vertex, DefaultEdge> exporter = new DOTExporter<>(x -> Integer.toString(x.intData()));
            StringWriter writer = new StringWriter();
            exporter.exportGraph(graph, writer);
            return writer.toString();
        }
    }
}
