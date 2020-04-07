package com.charrey.util;

import com.charrey.algorithms.CompatibilityChecker;
import com.charrey.algorithms.GreatestConstrainedFirst;
import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UtilityData {
    private final Graph<Vertex, DefaultEdge> targetGraph;
    private final Graph<Vertex, DefaultEdge> patternGraph;

    public UtilityData(Graph<Vertex, DefaultEdge> patternGraph, Graph<Vertex, DefaultEdge> targetGraph) {
        this.patternGraph = patternGraph;
        this.targetGraph = targetGraph;
    }

    private List<Vertex> order;
    public List<Vertex> getOrder() {
        if (order == null) {
            order = new GreatestConstrainedFirst().apply(patternGraph);
        }
        return order;
    }

    Map<Vertex, Set<Vertex>> compatibility;
    Map<Vertex, Set<Vertex>> getCompatibility() {
        if (compatibility == null) {
            compatibility = new CompatibilityChecker().get(patternGraph, targetGraph);
        }
        return compatibility;
    }


    private Map<Vertex, Vertex>[] toTryNext;
    public Map<Vertex, Vertex>[] getToTryNext() {
        if (toTryNext == null) {
            toTryNext = GraphUtil.getToTryNext(getOrder(), getCompatibility());
        }
        return toTryNext;
    }

}
