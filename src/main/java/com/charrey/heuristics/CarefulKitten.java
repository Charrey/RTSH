package com.charrey.heuristics;

import com.charrey.Cache;
import com.charrey.graph.AttributedVertex;
import com.charrey.graph.Path;
import com.charrey.util.GraphUtil;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public class CarefulKitten implements Heuristic{


    @Override
    public double get(Cache.CacheEntry entry, Graph<AttributedVertex, DefaultEdge> patternGraph, Graph<AttributedVertex, DefaultEdge> targetGraph) {
        Integer[] edgesMatched = GraphUtil.edgesMatched(patternGraph);
        int successfulEdges = edgesMatched[entry.placement.isEmpty() ? 0 : entry.placement.size() - 1];
        int cost = entry.addedPaths.stream().mapToInt(Path::length).sum();
        double costPerVertex = cost / (double) entry.placement.size();
        return -costPerVertex * (1 - (successfulEdges / (double) patternGraph.edgeSet().size()));
    }
}
