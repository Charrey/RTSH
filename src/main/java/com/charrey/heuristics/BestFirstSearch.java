package com.charrey.heuristics;

import com.charrey.Cache;
import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public class BestFirstSearch implements Heuristic {
    @Override
    public double get(Cache.CacheEntry entry, Graph<Vertex, DefaultEdge> patternGraph, Graph<Vertex, DefaultEdge> targetGraph) {
        return entry.placement.size();
    }
}
