package com.charrey.router;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;

import java.util.*;

public class PathIterator {
    private final Vertex b;

    private final Vertex[][] neighbours;
    private final int[] chosen;
    private final Path exploration;


    public PathIterator(Vertex[][] neighbours, Vertex a, Vertex b) {
        this.b = b;
        exploration = new Path(a, neighbours.length);
        this.neighbours = neighbours;
        chosen = new int[neighbours.length];
        Arrays.fill(chosen, 0);
    }


    public PathIterator(PathIterator pathIterator) {
        this.b = pathIterator.b;
        this.neighbours = pathIterator.neighbours;
        this.chosen = Arrays.copyOf(pathIterator.chosen, pathIterator.chosen.length);
        this.exploration = new Path(pathIterator.exploration);
    }

    public boolean hasNext() {
        return new PathIterator(this).next() != null;
    }


    public Path next() {
        if (exploration.head() == b) {
            chosen[exploration.length() - 2] += 1;
            exploration.removeHead();
        }
        while (exploration.head() != b) {
            int index = exploration.length() - 1;
            assert index < chosen.length;
            while (chosen[index] >= neighbours[exploration.get(index).intData()].length) {
                exploration.removeHead();
                if (exploration.isEmpty()) {
                    return null;
                }
                chosen[index] = 0;
                chosen[index - 1] += 1;
                index = exploration.length() - 1;
            }
            boolean found = false;
            //iterate over neighbours until we find an unused vertex
            for (int i = chosen[index]; i < neighbours[exploration.head().intData()].length; i++) {
                Vertex neighbour = neighbours[exploration.head().intData()][i];
                if (!exploration.contains(neighbour) && !Occupation.getOccupation(neighbour.getGraph()).isOccupiedRouting(neighbour)) {
                    //if found, update chosen, update exploration
                    exploration.append(neighbour);
                    chosen[index] = i;
                    found = true;
                    break;
                }
            }
            if (!found) {
                //if not found, bump previous index value.
                exploration.removeHead();
                if (exploration.isEmpty()) {
                    return null;
                }
                chosen[index] = 0;
                chosen[index - 1] += 1;
            }
        }
        assert !exploration.isEmpty();
        return exploration;
    }


}
