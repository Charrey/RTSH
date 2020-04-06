package com.charrey;

import com.charrey.graph.AttributedVertex;
import com.charrey.graph.Path;
import com.charrey.heuristics.Heuristic;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.function.Function;

public class Cache {

    private final Map<CacheEntry, List<CacheEntry>> cacheTree;
    private final SortedSet<CacheEntry> nextEntry;
    private final Set<Map<AttributedVertex, Integer>> colourCache;


    public Cache(Graph<AttributedVertex, DefaultEdge> patternGraph, Graph<AttributedVertex, DefaultEdge> targetGraph, Function<AttributedVertex, Integer> order, Heuristic heuristic) {
        nextEntry = new TreeSet<>(Comparator.comparingDouble(o -> heuristic.get(o, patternGraph, targetGraph)));
        colourCache = new HashSet<>();
        cacheTree = new HashMap<>();
        cacheTree.put(null, new LinkedList<>());
        add(Collections.emptyList(), null, Collections.emptySet(), false);
    }


    public CacheEntry add(List<AttributedVertex> placement, CacheEntry parent, Set<Path<AttributedVertex>> addedPaths, boolean worseAlternatives) {
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
        public final List<AttributedVertex> placement;
        public final Set<Path<AttributedVertex>> addedPaths;
        public final boolean worseAlternatives;


        private CacheEntry() {
            throw new UnsupportedOperationException();
        }


        public CacheEntry(List<AttributedVertex> placement, Set<Path<AttributedVertex>> addedPaths, boolean worseAlternatives) {
            this.placement = placement;
            this.addedPaths = addedPaths;
            this.worseAlternatives = worseAlternatives;
        }

    }
}
