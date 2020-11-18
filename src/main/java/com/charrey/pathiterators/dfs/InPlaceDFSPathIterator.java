package com.charrey.pathiterators.dfs;

import com.charrey.algorithms.RefuseLongerPaths;
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
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graphs;

import java.util.HashSet;
import java.util.Set;
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
        if (exploration.contains(vertex)) {
            return false;
        }
        if (occupation.isOccupiedRouting(vertex)) {
            return false;
        }
        if (occupation.isOccupiedVertex(vertex) && vertex != head) {
            return false;
        }
        if (!refuseLongerPaths) {
            return true;
        }
        Set<Integer> from;
        from = new HashSet<>();
        Set<Integer> finalFrom = from;
        exploration.asList().subList(0, exploration.length() - 1).forEachDescending(new TIntProcedure() {
            boolean inPrePhase = true;
            @Override
            public boolean execute(int value) {
                if (inPrePhase && graph.degreeOf(value) == 2) {
                    return true;
                } else {
                    inPrePhase = false;
                    finalFrom.add(value);
                    return true;
                }
            }
        });
        if (from.stream().anyMatch(x -> graph.getAllEdges(x, vertex).size() > 0)) {
            return false;
        } else return Graphs.predecessorListOf(graph, vertex)
                .stream()
                .filter(x -> graph.getLabels(x).contains("port") || graph.getLabels(x).contains("arc"))
                .filter(x -> graph.inDegreeOf(x) == 1)
                .filter(x -> graph.outDegreeOf(x) == 1)
                .filter(x -> x != exploration.last())
                .map(x -> Graphs.predecessorListOf(graph, x).iterator().next())
                .noneMatch(from::contains);
    }




    @Nullable
    @Override
    public Path getNext() {
        Path toReturn = null;
        while (toReturn == null) {
            ScalingIntList previouschosenoption = new ScalingIntList(nextOptionToTry);
            transaction.uncommit(placementSize.get(), this::getPartialMatching);
            if (exploration.length() > 1) {
                removeHeadOfExploration();
            }
            while (exploration.last() != head) {
                if (exploration.length() == 0) {
                    return null;
                }
                if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= timeoutTime) {
                    return null;
                }
                boolean foundCandidate = findCandidate();
                if (!foundCandidate) {
                    return null;
                }
                assert exploration.length() != 0;
            }
            assert !previouschosenoption.equals(nextOptionToTry);
            toReturn = commitAndReturn();
        }
        assert toReturn.length() >= 1;
        return toReturn;
    }


    private Path commitAndReturn() {
        try {
            transaction.commit(placementSize.get(), this::getPartialMatching);
            assert !exploration.isEmpty();
        } catch (DomainCheckerException | AssertionError e) {
            return null;
        }
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

    public TIntSet getLocallyOccupied() {
        return new TIntHashSet(exploration.intermediate().asList());
    }


    private void removeHeadOfExploration() {
        int removed = exploration.removeLast();
        if (!exploration.isEmpty() && removed != head) {
            transaction.releaseRouting(placementSize.get(), removed, this::getPartialMatching);
        }
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
