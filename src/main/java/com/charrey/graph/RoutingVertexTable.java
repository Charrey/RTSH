package com.charrey.graph;

import org.jgrapht.alg.util.Pair;

import java.util.*;

public class RoutingVertexTable<C extends Comparable<C>>  {

    private Map<C, Set<Option<C>>> options = new HashMap<>();

    public Option<C> addOption(C routingVertex) {
        if (routingVertex instanceof AttributedVertex) {
            assert ((AttributedVertex) routingVertex).containsLabel("routing");
        }
        options.putIfAbsent(routingVertex, new HashSet<>());
        Option<C> option = new Option<C>();
        options.get(routingVertex).add(option);
        return option;
    }

    public void addOptions(C routingVertex, C... neighbours) {
        for (int i = 0; i < neighbours.length-1; i++) {
            for (int j = i+1; j < neighbours.length; j++) {
                addOption(routingVertex).add(neighbours[i], neighbours[j], neighbours[j], neighbours[i]);
            }
        }

    }


    public static class Option<V extends Comparable<V>> {
        private Set<Pair<V, V>> connections = new HashSet<>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option<?> option = (Option<?>) o;
            return connections.equals(option.connections);
        }

        @Override
        public int hashCode() {
            return Objects.hash(connections);
        }

        public Option<V> add(V... vertices) {
            assert vertices.length%2==0;
            for (int i = 0; i < vertices.length; i+=2) {
                connections.add(new Pair<>(vertices[i], vertices[i + 1]));
            }
            return this;
        }

//        public void addSingleConnections(V... vertices) {
//            for (int i = 0; i < vertices.length-1; i++) {
//                for (int j = i+1; j < vertices.length; j++) {
//                    connections.add(new Pair<>(vertices[i], vertices[j]));
//                    connections.add(new Pair<>(vertices[j], vertices[i]));
//                }
//            }
//        }
    }
}
