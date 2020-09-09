package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.AbstractOccupation;
import com.charrey.util.Util;
import gnu.trove.set.hash.TIntHashSet;
import org.antlr.v4.runtime.misc.DoubleKeyMap;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;

import java.util.*;

public class MReachabilityFiltering implements FilteringSettings {

    private final FilteringSettings innerFilter = new UnmatchedDegreesFiltering();

    private boolean cached = false;

    public void setCached() {
        cached = true;
    }

    private final DoubleKeyMap<Integer, Integer, Path> pathCache = new DoubleKeyMap<>();
    private final Map<Integer, Set<Pair<Integer, Integer>>> reversePathLookup = new HashMap<>();

    @Override
    public boolean filter(MyGraph sourceGraph,
                          MyGraph targetGraph,
                          int sourceGraphVertex,
                          int targetGraphVertex,
                          AbstractOccupation occupation,
                          VertexMatching vertexMatching) {
        if (!innerFilter.filter(sourceGraph, targetGraph, sourceGraphVertex, targetGraphVertex, occupation, vertexMatching)) {
            return false;
        }
        Iterator<Integer> predecessorIterator = Graphs.predecessorListOf(sourceGraph, sourceGraphVertex).stream().filter(x -> x < sourceGraphVertex).iterator();
        while (predecessorIterator.hasNext()) {
            int predecessor = predecessorIterator.next();
            if (vertexMatching.getPlacement().size() <= predecessor) {
                continue;
            }
            if (cached && pathCache.get(vertexMatching.getPlacement().get(predecessor), targetGraphVertex) != null) {
                Path cached = pathCache.get(vertexMatching.getPlacement().get(predecessor), targetGraphVertex);
                if (cached.intermediate().stream().anyMatch(occupation::isOccupied)) {
                    removeFromCache(vertexMatching.getPlacement().get(predecessor), targetGraphVertex);
                    Optional<Path> path = Util.filteredShortestPath(targetGraph, occupation, new TIntHashSet(), vertexMatching.getPlacement().get(predecessor), targetGraphVertex, false, -1);
                    if (path.isEmpty()) {
                        return false;
                    } else {
                        addToCache(vertexMatching.getPlacement().get(predecessor), targetGraphVertex, path.get());
                    }
                }
            }
        }
        Iterator<Integer> successorIterator = Graphs.successorListOf(sourceGraph, sourceGraphVertex).stream().filter(x -> x < sourceGraphVertex).iterator();
        while (successorIterator.hasNext()) {
            int successor = successorIterator.next();
            if (vertexMatching.getPlacement().size() <= successor) {
                continue;
            }
            if (cached && pathCache.get(targetGraphVertex, vertexMatching.getPlacement().get(successor)) != null) {
                Path cached = pathCache.get(targetGraphVertex, vertexMatching.getPlacement().get(successor));
                if (cached.intermediate().stream().anyMatch(occupation::isOccupied)) {
                    removeFromCache(targetGraphVertex, vertexMatching.getPlacement().get(successor));
                    Optional<Path> path = Util.filteredShortestPath(targetGraph, occupation, new TIntHashSet(), targetGraphVertex, vertexMatching.getPlacement().get(successor), false, -1);
                    if (path.isEmpty()) {
                        return false;
                    } else {
                        addToCache(targetGraphVertex, vertexMatching.getPlacement().get(successor), path.get());
                    }
                }
            }
        }
        return true;
    }

    private void removeFromCache(int from, int to) {
        Path path = pathCache.get(from, to);
        pathCache.put(from, to, null);
        path.intermediate().forEach(integer -> reversePathLookup.get(integer).remove(new Pair<>(from, to)));
    }

    private void addToCache(int from, int to, Path path) {
        pathCache.put(from, to, path);
        Pair<Integer, Integer> pair = new Pair<>(from, to);
        path.intermediate().forEach(integer -> {
            reversePathLookup.computeIfAbsent(integer, x -> new HashSet<>());
            reversePathLookup.get(integer).add(pair);
        });
    }

    @Override
    public FilteringSettings newInstance() {
        MReachabilityFiltering toReturn =  new MReachabilityFiltering();
        if (cached) {
            toReturn.setCached();
        }
        return toReturn;
    }


}
