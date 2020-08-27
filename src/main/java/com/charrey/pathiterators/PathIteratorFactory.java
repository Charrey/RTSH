package com.charrey.pathiterators;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.CachedDFSPathIterator;
import com.charrey.pathiterators.dfs.InPlaceDFSPathIterator;
import com.charrey.pathiterators.kpath.KPathPathIterator;
import com.charrey.settings.Settings;
import com.charrey.settings.iterator.ControlPointIteratorStrategy;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PathIteratorFactory {
    /**
     * Get path iterator.
     *
     * @param targetGraph   the target graph
     * @param data          the data
     * @param tail          the tail
     * @param head          the head
     * @param occupation    the occupation
     * @param placementSize the placement size
     * @return the path iterator
     */
    @NotNull
    public static PathIterator get(@NotNull MyGraph targetGraph,
                                   @NotNull UtilityData data,
                                   int tail,
                                   int head,
                                   @NotNull GlobalOccupation occupation,
                                   Supplier<Integer> placementSize,
                                   Settings settings,
                                   PartialMatchingProvider provider,
                                   long timeoutTime,
                                   int crippled) {
        PathIterator toReturn;

        Set<MyEdge> removed = new HashSet<>();
        for (int i = 0; i < crippled; i++) {
            removed.add(targetGraph.removeEdge(tail, head));
        }

        if (targetGraph.getEdge(tail, head) != null) {
            toReturn = new SingletonPathIterator(targetGraph, settings, tail, head, provider, placementSize, crippled);
        } else if (tail == head) {
            toReturn = LoopAdaptor.get(targetGraph, settings, data, tail, occupation, placementSize, provider, timeoutTime, crippled);
        } else {
            switch (settings.getPathIteration().iterationStrategy) {
                case DFS_ARBITRARY, DFS_GREEDY:
                    if (settings.getDfsCaching()) {
                        toReturn = new CachedDFSPathIterator(targetGraph, settings, tail, head, occupation, placementSize, provider, data.getTargetNeighbours(settings.getPathIteration().iterationStrategy)[head], timeoutTime, crippled);
                    } else {
                        toReturn = new InPlaceDFSPathIterator(targetGraph, settings, tail, head, occupation, placementSize, provider, timeoutTime, crippled);
                    }
                    break;

                case CONTROL_POINT:
                    toReturn = new ManagedControlPointIterator(targetGraph, settings, tail, head, occupation, placementSize, provider, ((ControlPointIteratorStrategy) settings.getPathIteration()).getMaxControlpoints(), timeoutTime, crippled);
                    break;
                case KPATH:
                    toReturn = new KPathPathIterator(targetGraph, settings, tail, head, occupation, placementSize, provider, timeoutTime, crippled);
                    break;
                default:
                    throw new UnsupportedOperationException();
            };
        }
        removed.forEach(x -> targetGraph.addEdge(x.getSource(), x.getTarget(), x));
        return toReturn;
    }
}
