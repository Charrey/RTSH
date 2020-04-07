package com.charrey;

import com.charrey.graph.Vertex;
import com.charrey.graph.Path;
import com.charrey.heuristics.Heuristic;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.function.Function;

public class Cache {

    private final Map<CacheEntry, List<CacheEntry>> cacheTree;
    private final SortedSet<CacheEntry> nextEntry;
    private final Set<Map<Vertex, Integer>> colourCache;


    public Cache(Graph<Vertex, DefaultEdge> patternGraph, Graph<Vertex, DefaultEdge> targetGraph, Function<Vertex, Integer> order, Heuristic heuristic) {
        nextEntry = new TreeSet<>(Comparator.comparingDouble(o -> heuristic.get(o, patternGraph, targetGraph)));
        colourCache = new HashSet<>();
        cacheTree = new HashMap<>();
        cacheTree.put(null, new LinkedList<>());
        add(Collections.emptyList(), null, Collections.emptySet(), false);
    }


    public CacheEntry add(List<Vertex> placement, CacheEntry parent, Set<Path<Vertex>> addedPaths, boolean worseAlternatives) {
        cacheTree.putIfAbsent(parent, new LinkedList<>());
        CacheEntry toAdd = new CacheEntry(placement, addedPaths, worseAlternatives);
        cacheTree.get(parent).add(toAdd);
        nextEntry.add(toAdd);
        return toAdd;
    }

    public CacheEntry nextExploration() {
        return nextEntry.last();
    }



    public static class CacheEntry {

        //public static final CacheEntry ROOT = new CacheEntry(null, null, false);
        public final List<Vertex> placement;
        public final Set<Path<Vertex>> addedPaths;
        public final boolean worseAlternatives;


        private CacheEntry() {
            throw new UnsupportedOperationException();
        }


        public CacheEntry(List<Vertex> placement, Set<Path<Vertex>> addedPaths, boolean worseAlternatives) {
            this.placement = placement;
            this.addedPaths = addedPaths;
            this.worseAlternatives = worseAlternatives;
        }

    }
}
