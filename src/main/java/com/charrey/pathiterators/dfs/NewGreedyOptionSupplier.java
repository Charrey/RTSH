package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.util.Util;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.list.linked.TLinkedList;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class NewGreedyOptionSupplier extends OptionSupplier{

    private final GlobalOccupation occupation;

    NewGreedyOptionSupplier(GlobalOccupation occupation, MyGraph graph, int head) {
        super(graph, head);
        this.occupation = occupation;
    }

    @Override
    public int get(int at, int option, Path currentPath) {
        List<Integer> candidatesUnfiltered = Graphs.successorListOf(getGraph(), at);
        Map<Integer, Double> candidatesFiltered = new HashMap<>();
        candidatesUnfiltered.forEach(neighbour -> {
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
