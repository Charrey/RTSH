package com.charrey.router;

import com.charrey.graph.Vertex;
import com.charrey.graph.RoutingVertexTable;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collections;
import java.util.Set;

public class Router {

    public RoutingResult route(Vertex target, Set<Vertex> sources, Graph<Vertex, DefaultEdge> graph, RoutingVertexTable routingVertexTable, LockTable locked) {
        //Todo: I need the set of currently locked options
        //Todo: I need to know whether I need to deliver contested
        if (sources.isEmpty()) {
            return new RoutingResult(false, Collections.emptyList(), locked);
        }
        return RoutingResult.failed();
        //TODO: route
    }
}
