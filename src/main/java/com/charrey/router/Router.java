package com.charrey.router;

import com.charrey.graph.RoutingVertexTable;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collections;
import java.util.Set;

public class Router<V extends Comparable<V>> {

    @SuppressWarnings("unchecked")
    public RoutingResult<V> route(V target, Set<V> sources, Graph<V, DefaultEdge> graph, RoutingVertexTable<V> routingVertexTable, LockTable<V> locked) {
        //Todo: I need the set of currently locked options
        //Todo: I need to know whether I need to deliver contested
        if (sources.isEmpty()) {
            return new RoutingResult<V>(false, Collections.emptyList(), locked);
        }
        return RoutingResult.failed();
        //TODO: route
    }
}
