package com.charrey.heuristics;

import com.charrey.Cache;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public interface Heuristic<V1 extends Comparable<V1>, V2 extends Comparable<V2>> {


    double get(Cache.CacheEntry<V1, V2> entry, Graph<V1, DefaultEdge> patternGraph, Graph<V2, DefaultEdge> targetGraph);
}
