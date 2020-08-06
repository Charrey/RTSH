package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.IntVertexDijkstraShortestPath;

import java.util.*;

public class OldGreedyOptionSupplier extends OptionSupplier {
    private final long timeoutTime;

    public OldGreedyOptionSupplier(MyGraph graph, int head, long timeoutTime) {
        super(graph, head);
        this.timeoutTime = timeoutTime;
    }

    @Override
    public int get(int at, int option, Path currentPath) {
        final ShortestPathAlgorithm<Integer, MyEdge> spa = new IntVertexDijkstraShortestPath<>(getGraph());
        Map<Integer, Double> scores = new HashMap<>();
        Graphs.successorListOf(getGraph(), at).forEach(candidate -> {
            if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= timeoutTime) {
                return;
            }
            GraphPath<Integer, MyEdge> path = spa.getPath(candidate, getHead());
            if (path != null) {
                scores.put(candidate, path.getWeight() + getGraph().getEdgeWeight(getGraph().getEdge(at, candidate)));
            }
        });
        List<Integer> resList = new ArrayList<>(scores.keySet());

        if (option >= resList.size()) {
            return -1;
        }
        resList.sort(Comparator.comparingDouble(scores::get));
        return resList.get(option);
    }
}
