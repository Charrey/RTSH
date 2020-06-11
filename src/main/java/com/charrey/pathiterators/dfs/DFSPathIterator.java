package com.charrey.pathiterators.dfs;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.pathiterators.PathIterator;
import com.charrey.settings.Settings;
import com.charrey.util.datastructures.checker.DomainCheckerException;

import java.util.Arrays;
import java.util.function.Supplier;

public class DFSPathIterator extends PathIterator {
    private final Vertex head;

    private final Vertex[][] neighbours;
    private final int[] chosenOption;
    private final Path exploration;
    private final Occupation occupation;
    private final Supplier<Integer> placementSize;

    public DFSPathIterator(Vertex[][] neighbours, Vertex tail, Vertex head, Occupation occupation, Supplier<Integer> placementSize) {
        super(tail, head);
        this.head = head;
        exploration = new Path(tail, neighbours.length);
        this.neighbours = neighbours;
        chosenOption = new int[neighbours.length];
        Arrays.fill(chosenOption, 0);
        this.occupation = occupation;
        this.placementSize = placementSize;
    }

    private boolean isCandidate(Vertex from, Vertex vertex) {
        boolean isCandidate = !exploration.contains(vertex) &&
                !occupation.isOccupiedRouting(vertex) &&
                !(occupation.isOccupiedVertex(vertex) && vertex != head);
        if (Settings.instance.refuseLongerPaths) {
            isCandidate = isCandidate && exploration.stream().allMatch(x -> x == from || !Arrays.asList(neighbours[x.data()]).contains(vertex));
            //isCandidate = isCandidate && Arrays.stream(neighbours[vertex.data()]).noneMatch(x -> x != from && exploration.contains(x));
        }
        return isCandidate;
    }

    @Override
    public Path next() {
        assert !exploration.isEmpty();
        if (exploration.head() == head) {
            chosenOption[exploration.length() - 2] += 1;
            exploration.removeHead();
        }
        //assert exploration.length() < 2 || exploration.intermediate().stream().noneMatch(occupation::isOccupied);
        while (exploration.head() != head) {
            //System.out.println(exploration);
            int indexOfHeadVertex = exploration.length() - 1;
            assert indexOfHeadVertex < chosenOption.length;
            while (chosenOption[indexOfHeadVertex] >= neighbours[exploration.get(indexOfHeadVertex).data()].length) {
                Vertex removed = exploration.removeHead();
                if (exploration.isEmpty()) {
                    return null;
                } else {
                    occupation.releaseRouting(placementSize.get(), removed);
                }
                chosenOption[indexOfHeadVertex] = 0;
                chosenOption[indexOfHeadVertex - 1] += 1;
                indexOfHeadVertex = exploration.length() - 1;
            }
            boolean found = false;
            //iterate over neighbours until we find an unused vertex
            for (int i = chosenOption[indexOfHeadVertex]; i < neighbours[exploration.head().data()].length; i++) {
                Vertex neighbour = neighbours[exploration.head().data()][i];
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
                Vertex removed = exploration.removeHead();
                if (exploration.isEmpty()) {
                    return null;
                } else {
                    occupation.releaseRouting(placementSize.get(), removed);
                }
                chosenOption[indexOfHeadVertex] = 0;
                chosenOption[indexOfHeadVertex - 1] += 1;
            }
        }
        assert !exploration.isEmpty();
        //assert exploration.intermediate().stream().noneMatch(occupation::isOccupied);
        return exploration;
    }




    @Override
    public Vertex head() {
        return head;
    }

}

