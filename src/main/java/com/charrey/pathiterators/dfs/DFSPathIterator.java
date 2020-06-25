package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.PathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * A path iterator that performs DFS to obtain paths.
 */
public class DFSPathIterator extends PathIterator {
    private final int head;

    @NotNull
    private final int[][] outgoingNeighbours;
    @NotNull
    private final int[] chosenOption;
    @NotNull
    private final Path exploration;
    private final GlobalOccupation occupation;
    private final OccupationTransaction transaction;
    private final Supplier<Integer> placementSize;
    private int counter = 0;

    /**
     * Instantiates a new DFS path iterator.
     *
     * @param graph             the target graph
     * @param neighbours        integer array such that for any target graph vertex x, neighbours[x] is an array of                          outgoing neighbours in the order that they need to be attempted.
     * @param tail              the start vertex of the path
     * @param head              the end vertex of the path
     * @param occupation        the GlobalOccupation where intermediate nodes are registered
     * @param placementSize     supplier of the number of source graph vertices placed at this point in the search
     * @param refuseLongerPaths whether to refuse paths that use unnecessarily many resources.
     */
    public DFSPathIterator(@NotNull MyGraph graph, @NotNull int[][] neighbours, int tail, int head, GlobalOccupation occupation, Supplier<Integer> placementSize, boolean refuseLongerPaths) {
        super(tail, head, refuseLongerPaths);
        this.head = head;
        exploration = new Path(graph, tail);
        this.outgoingNeighbours = neighbours;
        chosenOption = new int[neighbours.length];
        Arrays.fill(chosenOption, 0);
        this.occupation = occupation;
        this.transaction = occupation.getTransaction();
        this.placementSize = placementSize;
    }

    private boolean isCandidate(int from, int vertex) {
        boolean isCandidate = !exploration.contains(vertex) &&
                !occupation.isOccupiedRouting(vertex) &&
                !(occupation.isOccupiedVertex(vertex) && vertex != head);
        if (refuseLongerPaths) {
            isCandidate = isCandidate && exploration.stream().allMatch(x -> x == from || Arrays.stream(outgoingNeighbours[x]).noneMatch(y -> y == vertex));
        }
        return isCandidate;
    }

    @Nullable
    @Override
    public Path next() {
        transaction.uncommit();
        assert !exploration.isEmpty();
        if (exploration.last() == head) {
            chosenOption[exploration.length() - 2] += 1;
            exploration.removeLast();
        }
        while (exploration.last() != head) {
            int indexOfHeadVertex = exploration.length() - 1;
            assert indexOfHeadVertex < chosenOption.length;
            while (chosenOption[indexOfHeadVertex] >= outgoingNeighbours[exploration.get(indexOfHeadVertex)].length) {
                if (removeHead(transaction)) {
                    return null;
                }
                indexOfHeadVertex = exploration.length() - 1;
            }
            boolean found = false;
            //iterate over neighbours until we find an unused vertex
            for (int i = chosenOption[indexOfHeadVertex]; i < outgoingNeighbours[exploration.last()].length; i++) {
                int neighbour = outgoingNeighbours[exploration.last()][i];
                if (isCandidate(exploration.last(), neighbour)) {
                    //if found, update chosen, update exploration
                    exploration.append(neighbour);
                    chosenOption[indexOfHeadVertex] = i;
                    found = true;
                    if (neighbour != head) {
                        try {
                            transaction.occupyRoutingAndCheck(this.placementSize.get(), neighbour);
                            break;
                        } catch (DomainCheckerException e) {
                            exploration.removeLast();
                            chosenOption[indexOfHeadVertex] = 0;
                            found = false;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (!found) {
                //if not found, bump previous index value.
                if (removeHead(transaction)) {
                    return null;
                }
            }
        }
        transaction.commit();
        assert !exploration.isEmpty();
        counter++;
        return exploration;
    }

    /**
     * Removes the head of the current exploration queue, provided that it's not the target vertex.
     *
     * @return whether the operation failed
     */
    private boolean removeHead(OccupationTransaction transaction) {
        int indexOfHeadVertex = exploration.length() - 1;
        int removed = exploration.removeLast();
        if (exploration.isEmpty()) {
            return true;
        } else {
            transaction.releaseRouting(placementSize.get(), removed);
        }
        chosenOption[indexOfHeadVertex] = 0;
        chosenOption[indexOfHeadVertex - 1] += 1;
        return false;
    }


    @Override
    public int head() {
        return head;
    }

    @Override
    public String debugInfo() {
        return String.valueOf(counter);
    }

}

