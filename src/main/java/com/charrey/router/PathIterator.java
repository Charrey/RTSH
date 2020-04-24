package com.charrey.router;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;

import java.util.Arrays;

public class PathIterator {
    private final Vertex head;
    private final Vertex tail;

    private final Vertex[][] neighbours;
    private final int[] chosen;
    private final Path exploration;
    private final Occupation occupation;
    private final int domainSize;


    public PathIterator(int domainSize, Vertex[][] neighbours, Vertex tail, Vertex head, Occupation occupation) {
        this.head = head;
        this.tail = tail;
        exploration = new Path(tail, neighbours.length);
        this.neighbours = neighbours;
        chosen = new int[neighbours.length];
        Arrays.fill(chosen, 0);
        this.occupation = occupation;
        this.domainSize = domainSize;
    }


    public PathIterator(PathIterator pathIterator) {
        this.head = pathIterator.head;
        this.tail = pathIterator.tail;
        this.neighbours = pathIterator.neighbours;
        this.chosen = Arrays.copyOf(pathIterator.chosen, pathIterator.chosen.length);
        this.exploration = new Path(pathIterator.exploration);
        this.occupation = new Occupation(pathIterator.occupation);
        this.domainSize = pathIterator.domainSize;
    }

    public boolean hasNext() {
        return new PathIterator(this).next() != null;
    }


    public Path next() {
        if (exploration.head() == head) {
            chosen[exploration.length() - 2] += 1;
            exploration.removeHead();
        }
        while (exploration.head() != head) {
            int index = exploration.length() - 1;
            assert index < chosen.length;
            while (chosen[index] >= neighbours[exploration.get(index).data()].length) {
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
            for (int i = chosen[index]; i < neighbours[exploration.head().data()].length; i++) {
                Vertex neighbour = neighbours[exploration.head().data()][i];
                if (!exploration.contains(neighbour) && !occupation.isOccupiedRouting(neighbour)) {
                    if (occupation.isOccupiedVertex(neighbour) && neighbour != head) {
                        continue;
                    }
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


    public Vertex tail() {
        return tail;
    }

    public Vertex head() {
        return head;
    }
}
