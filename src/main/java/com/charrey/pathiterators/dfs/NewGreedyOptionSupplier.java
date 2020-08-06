package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.util.Util;
import gnu.trove.set.hash.TIntHashSet;
import org.jgrapht.Graphs;

import java.util.*;

public class NewGreedyOptionSupplier extends OptionSupplier{

    private final GlobalOccupation occupation;
    private final long timeoutTime;

    NewGreedyOptionSupplier(GlobalOccupation occupation, MyGraph graph, int head, long timeoutTime) {
        super(graph, head);
        this.occupation = occupation;
        this.timeoutTime = timeoutTime;
    }

    @Override
    public int get(int at, int option, Path currentPath) {
        List<Integer> candidatesUnfiltered = Graphs.successorListOf(getGraph(), at);
        Map<Integer, Double> candidatesFiltered = new HashMap<>();
        candidatesUnfiltered.forEach(neighbour -> {
            if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= timeoutTime) {
                return;
            }
            Optional<Path> path = Util.filteredShortestPath(getGraph(), occupation, new TIntHashSet(currentPath.asList()), neighbour, getHead(), false, -1);
            path.ifPresent(realPath -> candidatesFiltered.put(neighbour, realPath.getWeight() + getGraph().getEdgeWeight(getGraph().getEdge(at, neighbour))));
        });
        List<Integer> candidatesFilteredList = new ArrayList<>(candidatesFiltered.keySet());
        candidatesFilteredList.sort(Comparator.comparingDouble(candidatesFiltered::get));
        if (option >= candidatesFilteredList.size()) {
            return -1;
        } else {
            return candidatesFilteredList.get(option);
        }
    }
}
