package com.charrey.example;

import com.charrey.graph.RoutingVertexTable;
import com.charrey.util.AnyGenerator;
import com.charrey.graph.AttributedVertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class GraphGenerator {

    private final static String alphabet = "abcdefghijklmnopqrstuvwxyz";


    @SuppressWarnings("unchecked")
    public static GraphGeneration<AttributedVertex<Character>> getTarget() {
        AnyGenerator<Integer> gen = new AnyGenerator<>(0, x -> ++x);
        Graph<AttributedVertex<Character>, DefaultEdge> target = new SimpleGraph<>(() -> new AttributedVertex<>(alphabet.charAt(gen.get())), DefaultEdge::new, false);
        AttributedVertex<Character> a = target.addVertex().addLabel("normal");
        AttributedVertex<Character> b = target.addVertex().addLabel("routing");
        AttributedVertex<Character> c = target.addVertex().addLabel("routing");
        AttributedVertex<Character> d = target.addVertex().addLabel("normal");
        AttributedVertex<Character> e = target.addVertex().addLabel("normal");
        AttributedVertex<Character> f = target.addVertex().addLabel("routing");
        AttributedVertex<Character> g = target.addVertex().addLabel("routing");
        AttributedVertex<Character> h = target.addVertex().addLabel("normal");
        AttributedVertex<Character> i = target.addVertex().addLabel("routing");
        AttributedVertex<Character> j = target.addVertex().addLabel("normal");
        AttributedVertex<Character> k = target.addVertex().addLabel("routing");
        AttributedVertex<Character> l = target.addVertex().addLabel("routing");
        AttributedVertex<Character> m = target.addVertex().addLabel("normal");
        AttributedVertex<Character> n = target.addVertex().addLabel("routing");
        AttributedVertex<Character> o = target.addVertex().addLabel("routing");
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
        RoutingVertexTable<AttributedVertex<Character>> routingVertexTable = new RoutingVertexTable<>();
        routingVertexTable.addOptions(b, f, a, c);
        routingVertexTable.addOptions(c, b, d, g);
        routingVertexTable.addOptions(f, e, b, g, k);
        routingVertexTable.addOptions(g, f, c, l, h);
        routingVertexTable.addOptions(i, h, n);
        routingVertexTable.addOptions(k, j, f, l);
        routingVertexTable.addOptions(l, k, g, m, o);
        routingVertexTable.addOptions(n, i, m, o);
        routingVertexTable.addOptions(o, l, m, n);
        return new GraphGeneration<AttributedVertex<Character>>(target, routingVertexTable);
    }

    public static Graph<AttributedVertex<Character>, DefaultEdge> getTarget2() {
        AnyGenerator<Integer> gen = new AnyGenerator<>(0, x -> ++x);
        Graph<AttributedVertex<Character>, DefaultEdge> target = new SimpleGraph<>(() -> new AttributedVertex<>(alphabet.charAt(gen.get())), DefaultEdge::new, false);
        AttributedVertex<Character> a = target.addVertex().addLabel("normal");
        AttributedVertex<Character> c = target.addVertex().addLabel("routing");
        AttributedVertex<Character> d = target.addVertex().addLabel("normal");
        AttributedVertex<Character> e = target.addVertex().addLabel("normal");
        AttributedVertex<Character> h = target.addVertex().addLabel("normal");
        AttributedVertex<Character> i = target.addVertex().addLabel("routing");
        AttributedVertex<Character> j = target.addVertex().addLabel("normal");
        AttributedVertex<Character> k = target.addVertex().addLabel("routing");
        AttributedVertex<Character> l = target.addVertex().addLabel("routing");
        AttributedVertex<Character> m = target.addVertex().addLabel("normal");
        AttributedVertex<Character> n = target.addVertex().addLabel("routing");
        AttributedVertex<Character> o = target.addVertex().addLabel("routing");
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

    public static GraphGeneration<AttributedVertex<Integer>> getPattern() {
        AnyGenerator<Integer> gen = new AnyGenerator<>(0, x -> ++x);
        Graph<AttributedVertex<Integer>, DefaultEdge> pattern = new SimpleGraph<>(() -> new AttributedVertex<>(gen.get()), DefaultEdge::new, false);
        AttributedVertex<Integer> p0 = pattern.addVertex().addLabel("normal");
        AttributedVertex<Integer> p1 = pattern.addVertex().addLabel("normal");
        AttributedVertex<Integer> p2 = pattern.addVertex().addLabel("normal");
        AttributedVertex<Integer> p3 = pattern.addVertex().addLabel("normal");
        AttributedVertex<Integer> p4 = pattern.addVertex().addLabel("normal");
        pattern.addEdge(p0, p1);
        pattern.addEdge(p0, p2);
        pattern.addEdge(p1, p3);
        pattern.addEdge(p2, p3);
        pattern.addEdge(p2, p4);
        pattern.addEdge(p3, p4);
        return new GraphGeneration<>(pattern, new RoutingVertexTable<>());
    }

    public static class GraphGeneration<T extends Comparable<T>> {
        public final Graph<T, DefaultEdge> graph;
        public final RoutingVertexTable<T> routingTable;

        public GraphGeneration(Graph<T, DefaultEdge> graph, RoutingVertexTable<T> routingTable) {
            this.graph = graph;
            this.routingTable = routingTable;
        }

        public Graph<T, DefaultEdge> getGraph() {
            return graph;
        }

        public RoutingVertexTable<T> getRoutingTable() {
            return routingTable;
        }
    }
}
