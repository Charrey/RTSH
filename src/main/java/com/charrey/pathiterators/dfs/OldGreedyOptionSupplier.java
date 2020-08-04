package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.GlobalOccupation;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.IntVertexDijkstraShortestPath;

import java.util.*;
import java.util.function.Consumer;

public class OldGreedyOptionSupplier extends OptionSupplier {
    public OldGreedyOptionSupplier(GlobalOccupation occupation, MyGraph graph, int head) {
        super(graph, head);
    }

    @Override
    public int get(int at, int option, Path currentPath) {
        final ShortestPathAlgorithm<Integer, MyEdge> spa = new IntVertexDijkstraShortestPath<>(getGraph());
        Map<Integer, Double> scores = new HashMap<>();
        Graphs.successorListOf(getGraph(), at).forEach(candidate -> {
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
