package com.charrey.pathiterators.dfs;

import com.charrey.graph.generation.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.PathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Supplier;

public class DFSPathIterator extends PathIterator {
    private final Vertex head;

    @NotNull
    private final Vertex[][] outgoingNeighbours;
    @NotNull
    private final int[] chosenOption;
    @NotNull
    private final Path exploration;
    private final GlobalOccupation occupation;
    private final OccupationTransaction transaction;
    private final Supplier<Integer> placementSize;
    private int counter = 0;

    public DFSPathIterator(@NotNull MyGraph graph, @NotNull Vertex[][] neighbours, @NotNull Vertex tail, Vertex head, GlobalOccupation occupation, Supplier<Integer> placementSize, boolean refuseLongerPaths) {
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

    private boolean isCandidate(Vertex from, @NotNull Vertex vertex) {
        boolean isCandidate = !exploration.contains(vertex) &&
                !occupation.isOccupiedRouting(vertex) &&
                !(occupation.isOccupiedVertex(vertex) && vertex != head);
        if (refuseLongerPaths) {
            isCandidate = isCandidate && exploration.stream().allMatch(x -> x == from || !Arrays.asList(outgoingNeighbours[x.data()]).contains(vertex));
            //isCandidate = isCandidate && Arrays.stream(neighbours[vertex.data()]).noneMatch(x -> x != from && exploration.contains(x));
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
            while (chosenOption[indexOfHeadVertex] >= outgoingNeighbours[exploration.get(indexOfHeadVertex).data()].length) {
                if (!removeHead(transaction)) {
                    return null;
                }
                indexOfHeadVertex = exploration.length() - 1;
            }
            boolean found = false;
            //iterate over neighbours until we find an unused vertex
            for (int i = chosenOption[indexOfHeadVertex]; i < outgoingNeighbours[exploration.last().data()].length; i++) {
                Vertex neighbour = outgoingNeighbours[exploration.last().data()][i];
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
                if (!removeHead(transaction)) {
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
     * @return whether the operation succeeded
     */
    private boolean removeHead(OccupationTransaction transaction) {
        int indexOfHeadVertex = exploration.length() - 1;
        Vertex removed = exploration.removeLast();
        if (exploration.isEmpty()) {
            return false;
        } else {
            transaction.releaseRouting(placementSize.get(), removed);
        }
        chosenOption[indexOfHeadVertex] = 0;
        chosenOption[indexOfHeadVertex - 1] += 1;
        return true;
    }


    @Override
    public Vertex head() {
        return head;
    }

    @Override
    public String debugInfo() {
        return String.valueOf(counter);
    }

}

