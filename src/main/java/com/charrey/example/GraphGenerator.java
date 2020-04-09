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

    private final static String alphabet = "abcdefghijklmnopqrstuvwxyz";


    public static GraphGeneration getTarget() {
        AnyGenerator<Integer> gen = new AnyGenerator<>(0, x -> ++x);
        Graph<Vertex, DefaultEdge> target = new SimpleGraph<>(() -> new Vertex(gen.get()), DefaultEdge::new, false);
        Vertex a = target.addVertex().addLabel("normal");
        Vertex b = target.addVertex().addLabel("routing");
        Vertex c = target.addVertex().addLabel("routing");
        Vertex d = target.addVertex().addLabel("normal");
        Vertex e = target.addVertex().addLabel("normal");
        Vertex f = target.addVertex().addLabel("routing");
        Vertex g = target.addVertex().addLabel("routing");
        Vertex h = target.addVertex().addLabel("normal");
        Vertex i = target.addVertex().addLabel("routing");
        Vertex j = target.addVertex().addLabel("normal");
        Vertex k = target.addVertex().addLabel("routing");
        Vertex l = target.addVertex().addLabel("routing");
        Vertex m = target.addVertex().addLabel("normal");
        Vertex n = target.addVertex().addLabel("routing");
        Vertex o = target.addVertex().addLabel("routing");
        target.addEdge(a, b);
        target.addEdge(a, e);
        target.addEdge(b, c);
        target.addEdge(b, f);
        target.addEdge(c, d);
        target.addEdge(c, g);
        target.addEdge(d, h);
        target.addEdge(e, f);
        target.addEdge(e, j);
        target.addEdge(f, g);
        target.addEdge(f, k);
        target.addEdge(g, h);
        target.addEdge(g, l);
        target.addEdge(h, i);
        target.addEdge(h, m);
        target.addEdge(i, n);
        target.addEdge(j, k);
        target.addEdge(k, l);
        target.addEdge(l, m);
        target.addEdge(l, o);
        target.addEdge(m, n);
        target.addEdge(m, o);
        target.addEdge(n, o);
        RoutingVertexTable routingVertexTable = new RoutingVertexTable();
        routingVertexTable.addOptions(b, f, a, c);
        routingVertexTable.addOptions(c, b, d, g);
        routingVertexTable.addOptions(f, e, b, g, k);
        routingVertexTable.addOptions(g, f, c, l, h);
        routingVertexTable.addOptions(i, h, n);
        routingVertexTable.addOptions(k, j, f, l);
        routingVertexTable.addOptions(l, k, g, m, o);
        routingVertexTable.addOptions(n, i, m, o);
        routingVertexTable.addOptions(o, l, m, n);
        return new GraphGeneration(target, routingVertexTable);
    }

    public static Graph<Vertex, DefaultEdge> getTarget2() {
        AnyGenerator<Integer> gen = new AnyGenerator<>(0, x -> ++x);
        Graph<Vertex, DefaultEdge> target = new SimpleGraph<>(() -> new Vertex(alphabet.charAt(gen.get())), DefaultEdge::new, false);
        Vertex a = target.addVertex().addLabel("normal");
        Vertex c = target.addVertex().addLabel("routing");
        Vertex d = target.addVertex().addLabel("normal");
        Vertex e = target.addVertex().addLabel("normal");
        Vertex h = target.addVertex().addLabel("normal");
        Vertex i = target.addVertex().addLabel("routing");
        Vertex j = target.addVertex().addLabel("normal");
        Vertex k = target.addVertex().addLabel("routing");
        Vertex l = target.addVertex().addLabel("routing");
        Vertex m = target.addVertex().addLabel("normal");
        Vertex n = target.addVertex().addLabel("routing");
        Vertex o = target.addVertex().addLabel("routing");
        target.addEdge(a, c);
        target.addEdge(a, e);
        target.addEdge(c, d);
        target.addEdge(d, h);
        target.addEdge(e, c);
        target.addEdge(e, j);
        target.addEdge(c, h);
        target.addEdge(h, i);
        target.addEdge(h, m);
        target.addEdge(i, n);
        target.addEdge(j, k);
        target.addEdge(k, l);
        target.addEdge(l, m);
        target.addEdge(l, o);
        target.addEdge(m, n);
        target.addEdge(m, o);
        target.addEdge(n, o);
        return target;
    }

    public static GraphGeneration getPattern() {
        AnyGenerator<Integer> gen = new AnyGenerator<>(0, x -> ++x);
        Graph<Vertex, DefaultEdge> pattern = new SimpleGraph<>(() -> new Vertex(gen.get()), DefaultEdge::new, false);
        Vertex p0 = pattern.addVertex().addLabel("normal");
        Vertex p1 = pattern.addVertex().addLabel("normal");
        Vertex p2 = pattern.addVertex().addLabel("normal");
        Vertex p3 = pattern.addVertex().addLabel("normal");
        Vertex p4 = pattern.addVertex().addLabel("normal");
        pattern.addEdge(p0, p1);
        pattern.addEdge(p0, p2);
        pattern.addEdge(p1, p3);
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
