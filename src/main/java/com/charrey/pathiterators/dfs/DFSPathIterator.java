package com.charrey.pathiterators.dfs;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
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
    private final Occupation occupation;
    private final Supplier<Integer> placementSize;

    public DFSPathIterator(@NotNull Vertex[][] neighbours, @NotNull Vertex tail, Vertex head, Occupation occupation, Supplier<Integer> placementSize, boolean refuseLongerPaths) {
        super(tail, head, refuseLongerPaths);
        this.head = head;
        exploration = new Path(tail, neighbours.length);
        this.outgoingNeighbours = neighbours;
        chosenOption = new int[neighbours.length];
        Arrays.fill(chosenOption, 0);
        this.occupation = occupation;
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
        assert !exploration.isEmpty();
        if (exploration.head() == head) {
            chosenOption[exploration.length() - 2] += 1;
            exploration.removeHead();
        }
        while (exploration.head() != head) {
            int indexOfHeadVertex = exploration.length() - 1;
            assert indexOfHeadVertex < chosenOption.length;
            while (chosenOption[indexOfHeadVertex] >= outgoingNeighbours[exploration.get(indexOfHeadVertex).data()].length) {
                if (!removeHead()) {
                    return null;
                }
                indexOfHeadVertex = exploration.length() - 1;
            }
            boolean found = false;
            //iterate over neighbours until we find an unused vertex
            for (int i = chosenOption[indexOfHeadVertex]; i < outgoingNeighbours[exploration.head().data()].length; i++) {
                Vertex neighbour = outgoingNeighbours[exploration.head().data()][i];
                if (isCandidate(exploration.head(), neighbour)) {
                    //if found, update chosen, update exploration
                    exploration.append(neighbour);
                    chosenOption[indexOfHeadVertex] = i;
                    found = true;
                    if (neighbour != head) {
                        try {
                            occupation.occupyRoutingAndCheck(this.placementSize.get(), neighbour);
                            break;
                        } catch (DomainCheckerException e) {
                            exploration.removeHead();
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
                if (!removeHead()) {
                    return null;
                }
            }
        }
        assert !exploration.isEmpty();
        return exploration;
    }

    /**
     * Removes the head of the current exploration queue, provided that it's not the target vertex.
     * @return whether the operation succeeded
     */
    private boolean removeHead() {
        int indexOfHeadVertex = exploration.length() - 1;
        Vertex removed = exploration.removeHead();
        if (exploration.isEmpty()) {
            return false;
        } else {
            occupation.releaseRouting(placementSize.get(), removed);
        }
        chosenOption[indexOfHeadVertex] = 0;
        chosenOption[indexOfHeadVertex - 1] += 1;
        return true;
    }


    @Override
    public Vertex head() {
        return head;
    }

}

