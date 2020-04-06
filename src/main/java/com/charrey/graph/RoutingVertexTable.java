package com.charrey.graph;

import org.jgrapht.alg.util.Pair;

import java.util.*;

public class RoutingVertexTable {

    private Map<AttributedVertex, Set<Option>> options = new HashMap<>();

    public Option addOption(AttributedVertex routingVertex) {
        assert routingVertex.containsLabel("routing");
        options.putIfAbsent(routingVertex, new HashSet<>());
        Option option = new Option();
        options.get(routingVertex).add(option);
        return option;
    }

    public void addOptions(AttributedVertex routingVertex, AttributedVertex... neighbours) {
        for (int i = 0; i < neighbours.length-1; i++) {
            for (int j = i+1; j < neighbours.length; j++) {
                addOption(routingVertex).add(neighbours[i], neighbours[j], neighbours[j], neighbours[i]);
            }
        }

    }


    public static class Option {
        private Set<Pair<AttributedVertex, AttributedVertex>> connections = new HashSet<>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
            return connections.equals(option.connections);
        }

        @Override
        public int hashCode() {
            return Objects.hash(connections);
        }

        public Option add(AttributedVertex... vertices) {
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
