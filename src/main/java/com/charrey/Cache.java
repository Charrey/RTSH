package com.charrey;

import com.charrey.graph.Path;
import com.charrey.heuristics.Heuristic;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.function.Function;

public class Cache<V1 extends Comparable<V1>, V2 extends Comparable<V2>> {

    private final Map<CacheEntry<V1, V2>, List<CacheEntry<V1, V2>>> cacheTree;
    private final SortedSet<CacheEntry<V1, V2>> nextEntry;
    private final Set<Map<V2, Integer>> colourCache;


    public Cache(Graph<V1, DefaultEdge> patternGraph, Graph<V2, DefaultEdge> targetGraph, Function<V1, Integer> order, Heuristic<V1, V2> heuristic) {
        nextEntry = new TreeSet<>(Comparator.comparingDouble(o -> heuristic.get(o, patternGraph, targetGraph)));
        colourCache = new HashSet<>();
        cacheTree = new HashMap<>();
        cacheTree.put(null, new LinkedList<>());
        add(Collections.emptyList(), null, Collections.emptySet(), false);
    }


    public CacheEntry<V1, V2> add(List<V2> placement, CacheEntry<V1, V2> parent, Set<Path<V2>> addedPaths, boolean worseAlternatives) {
        cacheTree.putIfAbsent(parent, new LinkedList<>());
        CacheEntry<V1, V2> toAdd = new CacheEntry<>(placement, addedPaths, worseAlternatives);
        cacheTree.get(parent).add(toAdd);
        nextEntry.add(toAdd);
        return toAdd;
    }

    public CacheEntry<V1, V2> nextExploration() {
        return nextEntry.last();
    }



    public static class CacheEntry<V1 extends Comparable<V1>, V2 extends Comparable<V2>> {

        //public static final CacheEntry ROOT = new CacheEntry(null, null, false);
        public final List<V2> placement;
        public final Set<Path<V2>> addedPaths;
        public final boolean worseAlternatives;


        private CacheEntry() {
            throw new UnsupportedOperationException();
        }


        public CacheEntry(List<V2> placement, Set<Path<V2>> addedPaths, boolean worseAlternatives) {
            this.placement = placement;
            this.addedPaths = addedPaths;
            this.worseAlternatives = worseAlternatives;
        }

    }
}
