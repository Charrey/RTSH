package com.charrey.pathiterators.dfs;

import com.charrey.algorithms.RefuseLongerPaths;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.settings.Settings;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.function.Supplier;

/**
 * A path iterator that performs DFS to obtain paths.
 * Space requirements (globally) for using this:
 * O(|E1|* (|E2|+|V2|))
 */
public class CachedDFSPathIterator extends PathIterator {
    private final int head;

    @NotNull
    private final Supplier<int[][]> outgoingNeighbours;
    @NotNull
    private final int[] chosenOption;
    @NotNull
    private final Path exploration;

    private final GlobalOccupation occupation;
    private final Supplier<Integer> placementSize;
    private final MyGraph graph;


    /**
     * Instantiates a new DFS path iterator.
     *  @param graph         the target graph
     * @param tail          the start vertex of the path
     * @param head          the end vertex of the path
     * @param occupation    the GlobalOccupation where intermediate nodes are registered
     * @param placementSize supplier of the number of source graph vertices placed at this point in the search
     * @param neighbours    integer array such that for any target graph vertex x, neighbours[x] is an array of                          outgoing neighbours in the order that they need to be attempted.
     * @param timeoutTime
     */
    public CachedDFSPathIterator(@NotNull MyGraph graph,
                                 Settings settings,
                                 int tail,
                                 int head,
                                 GlobalOccupation occupation,
                                 Supplier<Integer> placementSize,
                                 PartialMatchingProvider provider,
                                 @NotNull Supplier<int[][]> neighbours,
                                 long timeoutTime, int cripple) {
        super(graph, tail, head, settings, occupation, occupation.getTransaction(), provider, timeoutTime, placementSize, cripple);
        this.head = head;
        exploration = new Path(graph, tail);
        this.outgoingNeighbours = neighbours;
        chosenOption = new int[graph.vertexSet().size()];
        Arrays.fill(chosenOption, 0);
        this.occupation = occupation;
        this.placementSize = placementSize;
        this.graph = graph;
        this.timeoutTime = timeoutTime;
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

    @Override
    public TIntSet getLocallyOccupied() {
        return new TIntHashSet(exploration.subPath(1, exploration.length()).asList());
    }

    @Nullable
    @Override
    public Path getNext() {
        transaction.uncommit(placementSize.get(), this::getPartialMatching);
        if (exploration.length() > 1) {
            removeHead();
        }
        while (exploration.last() != head) {
            if (backtrackExhaustedOptions()) {
                return null;
            }
            int indexOfHeadVertex = exploration.length() - 1;
            boolean foundCandidate = findCandidate(indexOfHeadVertex);
            if (!foundCandidate && !removeHead()) {
                return null;
            }
        }
        return commitAndReturn();
    }

    private boolean findCandidate(int indexOfHeadVertex) {
        boolean foundCandidate = false;
        for (int i = chosenOption[indexOfHeadVertex]; i < outgoingNeighbours.get()[exploration.last()].length; i++) {
            if (!graph.containsEdge(exploration.last(), outgoingNeighbours.get()[exploration.last()][i])) { //initial cache is without cripple
                continue;
            }
            int neighbour = outgoingNeighbours.get()[exploration.last()][i];
            if (isCandidate(neighbour)) {
                exploration.append(neighbour);
                chosenOption[indexOfHeadVertex] = i;
                if (occupyCandidate(indexOfHeadVertex, neighbour)) {
                    foundCandidate = true;
                    break;
                }
            }
        }
        return foundCandidate;
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

    private boolean backtrackExhaustedOptions() {
        int foo = exploration.length() - 1;
        while (chosenOption[foo] >= outgoingNeighbours.get()[exploration.get(foo)].length) {
            if (!removeHead()) {
                return true;
            }
            foo = exploration.length() - 1;
        }
        return false;
    }

    private boolean occupyCandidate(int indexOfHeadVertex, int newHead) {
        boolean foundCandidate;
        if (newHead != head) {
            try {
                transaction.occupyRoutingAndCheck(this.placementSize.get(), newHead, this::getPartialMatching);
                foundCandidate = true;
            } catch (DomainCheckerException e) {
                exploration.removeLast();
                chosenOption[indexOfHeadVertex] = 0;
                foundCandidate = false;
            }
        } else {
            foundCandidate = true;
        }
        return foundCandidate;
    }



    /**
     * Removes the head of the current exploration queue, provided that it's not the target vertex.
     *
     * @return whether the operation succeeded
     */
    private boolean removeHead() {
        int removed = exploration.removeLast();
        if (exploration.isEmpty()) {
            return false;
        } else if (removed != head) {
            transaction.releaseRouting(placementSize.get(), removed, this::getPartialMatching);
            chosenOption[exploration.length()] = 0;
        }
        chosenOption[exploration.length() - 1] += 1;
        return true;
    }


    @Override
    public int head() {
        return head;
    }

    @Override
    public String debugInfo() {
        return "";
    }

}

