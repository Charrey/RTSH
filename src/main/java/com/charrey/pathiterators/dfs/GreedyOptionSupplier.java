package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.util.Util;
import gnu.trove.set.hash.TIntHashSet;
import org.jgrapht.Graphs;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class GreedyOptionSupplier extends OptionSupplier{

    private final GlobalOccupation occupation;

    GreedyOptionSupplier(GlobalOccupation occupation, MyGraph graph, int head) {
        super(graph, head);
        this.occupation = occupation;
    }

    @Override
    public int get(int at, int option, Path currentPath) {
        List<Integer> candidates = Graphs.successorListOf(getGraph(), at);
        AtomicInteger validOptions = new AtomicInteger();
        candidates.sort(Comparator.comparingDouble(neighbour -> {
            Optional<Path> path = Util.filteredShortestPath(getGraph(), occupation, new TIntHashSet(currentPath.asList()), neighbour, getHead(), false, -1);
            if (path.isPresent()) {
                validOptions.getAndIncrement();
                return path.get().getWeight();
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }));
        if (option + 1 > validOptions.get()) {
            return -1;
        } else {
            return candidates.get(option);
        }
    }
}
