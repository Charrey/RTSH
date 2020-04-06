package com.charrey.heuristics;

import com.charrey.Cache;
import com.charrey.graph.AttributedVertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public interface Heuristic {


    double get(Cache.CacheEntry entry, Graph<AttributedVertex, DefaultEdge> patternGraph, Graph<AttributedVertex, DefaultEdge> targetGraph);
}
