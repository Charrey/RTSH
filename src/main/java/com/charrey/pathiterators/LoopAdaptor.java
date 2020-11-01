package com.charrey.pathiterators;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.settings.Settings;
import gnu.trove.set.TIntSet;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Supplier;

public class LoopAdaptor extends PathIterator {

    private final MyGraph graph;
    private final int tailHead;

    double[] distances;
    int[] neighbours;
    PriorityQueue<Pair<Integer, Path>> pathQueue;
    List<PathIterator> spas;



    public static PathIterator get(MyGraph targetGraph,
                       Settings settings,
                       UtilityData data,
                       int tailHead,
                       GlobalOccupation occupation,
                       Supplier<Integer> placementSize,
                       PartialMatchingProvider provider,
                       long timeoutTime, int cripple) {
        OccupationTransaction tempTransaction = occupation.getTransaction();
        int[] neighbours = Graphs.successorListOf(targetGraph, tailHead).stream().filter(x -> x != tailHead && canOccupyRouting(tempTransaction, x, provider, placementSize)).mapToInt(x -> x).toArray();
        if (neighbours.length == 0) {
            return new VoidPathIterator(settings);
        } else {
            return new LoopAdaptor(targetGraph, settings, data, tailHead, occupation, placementSize, provider, timeoutTime, neighbours, cripple);
        }
    }


    private LoopAdaptor(MyGraph targetGraph,
                       Settings settings,
                       UtilityData data,
                       int tailHead,
                       GlobalOccupation occupation,
                       Supplier<Integer> placementSize,
                       PartialMatchingProvider provider,
                       long timeoutTime,
                        int[] neighbours,
                        int cripple) {
        super(targetGraph, tailHead, tailHead, settings, occupation, occupation.getTransaction(), provider, timeoutTime, placementSize, cripple);
        this.graph = targetGraph;
        this.tailHead = tailHead;
        this.neighbours = neighbours;
        distances = new double[neighbours.length];
        spas = new ArrayList<>(neighbours.length);
        pathQueue = new PriorityQueue<>(neighbours.length, Comparator.comparingDouble(o -> distances[o.getFirst()] + o.getSecond().getWeight()));
        for (int i = 0; i < neighbours.length; i++) {
            distances[i] = graph.getEdgeWeight(graph.getEdge(tailHead, neighbours[i]));
            PathIterator pathIterator = PathIteratorFactory.get(graph, data, neighbours[i], tailHead, occupation, placementSize, settings, provider, timeoutTime, 0, -1, -1);
            spas.add(pathIterator);
            Path path = pathIterator.next();
            pathIterator.uncommit();
            if (path != null) {
                assert path.length() >= 1;
                pathQueue.add(new Pair<>(i, new Path(path)));
            }
        }
    }

    private static boolean canOccupyRouting(OccupationTransaction transaction, Integer toOccupy, PartialMatchingProvider partialMatchingProvider, Supplier<Integer> placementSize) {
        if (transaction.isOccupied(toOccupy)) {
            return false;
        } else {
            try {
                transaction.occupyRoutingAndCheck(placementSize.get(), toOccupy, partialMatchingProvider);
                transaction.releaseRouting(placementSize.get(), toOccupy, partialMatchingProvider);
                return true;
            } catch (DomainCheckerException e) {
                return false;
            }
        }
    }

    @Override
    public TIntSet getLocallyOccupied() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Path getNext() {
        transaction.uncommit(placementSize.get(), this::getPartialMatching);
        transaction = this.globalOccupation.getTransaction();
        if (pathQueue.isEmpty()) {
            return null;
        } else {
            Pair<Integer, Path> next = pathQueue.poll();
            Path nextPath = spas.get(next.getFirst()).next();
            spas.get(next.getFirst()).uncommit();
            if (nextPath != null) {
                assert nextPath.length() >= 1;
                pathQueue.add(new Pair<>(next.getFirst(), nextPath));
            }
            Path path = next.getSecond();

            assert path.length() >= 1;
            path.insertUnsafe(0, tailHead);
            try {
                transaction.occupyRoutingAndCheck(placementSize.get(), path, partialMatchingProvider);
                transaction.commit(placementSize.get() , partialMatchingProvider);
            } catch (DomainCheckerException e) {
                return getNext();
            }
            return path;
        }
    }

    @Override
    public String debugInfo() {
        return "Loopadaptor";
    }
}
