package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.settings.iterator.DFSStrategy;
import com.charrey.settings.iterator.IteratorSettings;
import com.charrey.settings.iterator.NewGreedyDFSStrategy;
import com.charrey.settings.iterator.OldGreedyDFSStrategy;
import com.charrey.settings.Settings;
import com.charrey.util.datastructures.ScalingIntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graphs;

import java.util.Arrays;
import java.util.function.Supplier;

public class InPlaceDFSPathIterator extends PathIterator {

    private final int head;
    private final Path exploration;
    public final ScalingIntList nextOptionToTry; //at index 0, displays options to choose from FROM exploration index 0.
    private final GlobalOccupation occupation;
    private final Supplier<Integer> placementSize;
    private final MyGraph graph;

    private final OptionSupplier optionSupplier;

    public InPlaceDFSPathIterator(@NotNull MyGraph graph,
                                  Settings settings,
                                  int tail,
                                  int head,
                                  GlobalOccupation occupation,
                                  Supplier<Integer> placementSize,
                                  PartialMatchingProvider provider,
                                  long timeoutTime, int cripple) {
        super(graph, tail, head, settings, occupation, occupation.getTransaction(), provider, timeoutTime, placementSize, cripple);
        this.head = head;
        this.exploration = new Path(graph, tail);
        this.nextOptionToTry = new ScalingIntList();
        this.occupation = occupation;
        this.placementSize = placementSize;
        this.graph = graph;
        IteratorSettings type = settings.getPathIteration();
        if (type instanceof DFSStrategy) {
            this.optionSupplier = new IndexOptionSupplier(graph, head);
        } else if (type instanceof NewGreedyDFSStrategy){
            this.optionSupplier = new NewGreedyOptionSupplier(occupation, graph, head, timeoutTime);
        } else if (type instanceof OldGreedyDFSStrategy) {
            this.optionSupplier = new OldGreedyOptionSupplier(graph, head, timeoutTime);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private boolean isCandidate(Integer vertex) {
        return (vertex == head || !graph.containsEdge(exploration.last(), head)) && !exploration.contains(vertex) &&
                !occupation.isOccupiedRouting(vertex) &&
                !(occupation.isOccupiedVertex(vertex) && vertex != head) &&
                (!refuseLongerPaths || Graphs.predecessorListOf(graph, vertex).stream().allMatch(x -> x == exploration.last() || !exploration.contains(x)));
    }



    Path lastReturned = null;
    @Nullable
    @Override
    public Path getNext() {
        ScalingIntList previouschosenoption = new ScalingIntList(nextOptionToTry);
        transaction.uncommit(placementSize.get());
        if (exploration.length() > 1) {
            removeHeadOfExploration();
        }
        while (exploration.last() != head) {
            assert exploration.length() != 0;
            if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= timeoutTime) {
                return null;
            }
            boolean foundCandidate = findCandidate();
            if (!foundCandidate) {
                return null;
            }
            assert exploration.length() != 0;
        }
        assert Arrays.stream(nextOptionToTry.toArray()).anyMatch(x -> x != 0);
        assert !previouschosenoption.equals(nextOptionToTry);
        Path toReturn = commitAndReturn();
        //assert !toReturn.equals(lastReturned) : "Path returned multiple times: " + lastReturned;
        lastReturned = new Path(toReturn);
        assert toReturn.length() >= 1;
        return toReturn;
    }


    private Path commitAndReturn() {
        try {
            transaction.commit(placementSize.get(), this::getPartialMatching);
        } catch (DomainCheckerException e) {
            return next();
        }
        assert !exploration.isEmpty();
        return exploration;
    }

    private boolean findCandidate() {
        assert exploration.length() != 0;
        while (true) {
            if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= timeoutTime) {
                return false;
            }
            int option = nextOptionToTry.get(exploration.length() - 1);
            int neighbour = this.optionSupplier.get(exploration.last(), option, exploration);
            if (neighbour == -1) {
                nextOptionToTry.set(exploration.length() - 1,  0);
                removeHeadOfExploration();
                if (exploration.isEmpty()) {
                    return false;
                }
            } else if (isCandidate(neighbour)) {
                nextOptionToTry.set(exploration.length() - 1,  option + 1);
                exploration.append(neighbour);
                if (occupyCandidate(neighbour)) {
                    return true;
                } else {
                    if (exploration.isEmpty()) {
                        return false;
                    }
                }
            } else {
                nextOptionToTry.set(exploration.length() - 1,  option + 1);
            }
        }
    }

    private boolean occupyCandidate(int newHead) {
        boolean foundCandidate;
        if (newHead != head) {
            try {
                transaction.occupyRoutingAndCheck(this.placementSize.get(), newHead, this::getPartialMatching);
                foundCandidate = true;
            } catch (DomainCheckerException e) {
                exploration.removeLast();
                foundCandidate = false;
            }
        } else {
            foundCandidate = true;
        }
        return foundCandidate;
    }


    private boolean removeHeadOfExploration() {
        int removed = exploration.removeLast();
        if (exploration.isEmpty()) {
            return false;
        } else if (removed != head) {
            transaction.releaseRouting(placementSize.get(), removed);
        }
        return true;
    }





    /**
     * Debug info string.
     *
     * @return the string
     */
    @Override
    public String debugInfo() {
        return null;
    }
}
