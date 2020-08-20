package com.charrey.pathiterators;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;
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
    PathIterator inner;

    double[] distances;
    int[] neighbours;
    PriorityQueue<Pair<Integer, Path>> pathQueue;
    List<PathIterator> spas;


    public LoopAdaptor(MyGraph targetGraph,
                       Settings settings,
                       UtilityData data,
                       int tailHead,
                       GlobalOccupation occupation,
                       Supplier<Integer> placementSize,
                       PartialMatchingProvider provider,
                       long timeoutTime) {
        super(tailHead, tailHead, settings, occupation, occupation.getTransaction(), provider, timeoutTime, placementSize);
        this.graph = targetGraph;
        this.tailHead = tailHead;
        neighbours = Graphs.successorListOf(graph, tailHead).stream().filter(x -> x != tailHead).mapToInt(x -> x).toArray();
        distances = new double[neighbours.length];
        spas = new ArrayList<>(neighbours.length);
        pathQueue = new PriorityQueue<>(neighbours.length, Comparator.comparingDouble(o -> distances[o.getFirst()] + o.getSecond().getWeight()));
        for (int i = 0; i < neighbours.length; i++) {
            distances[i] = graph.getEdgeWeight(graph.getEdge(tailHead, neighbours[i]));
            PathIterator pathIterator = PathIteratorFactory.get(graph, data, neighbours[i], tailHead, occupation, placementSize, settings, provider, timeoutTime);
            spas.add(pathIterator);
            Path path = pathIterator.next();
            pathIterator.uncommit();
            if (path != null) {
                assert path.length() >= 1;
                pathQueue.add(new Pair<>(i, new Path(path)));
            }
        }
    }

    @Nullable
    @Override
    public Path getNext() {
        uncommit();
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
                assert false;
            }
            return path;
        }
    }

    @Override
    public String debugInfo() {
        return "Loopadaptor";
    }
}
