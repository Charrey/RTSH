package com.charrey.router;

import com.charrey.example.GraphGenerator;
import com.charrey.graph.Path;
import com.charrey.graph.RoutingVertexTable;
import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class PathIterator implements Iterator<Path> {
    private final Vertex b;
    private final Vertex a;
    private final RoutingVertexTable routingTable;
    private final Graph<Vertex, DefaultEdge> graph;

    private final Vertex[][] neighbours;
    private final int[] chosen;


    public PathIterator(GraphGenerator.GraphGeneration graph, Vertex[][] neighbours, Vertex a, Vertex b) {
        this.a = a;
        this.b = b;
        exploration = new Path(a);
        this.graph = graph.getGraph();
        this.routingTable = graph.getRoutingTable();
        this.neighbours = neighbours;
        chosen = new int[neighbours.length];
        Arrays.fill(chosen, 0);
    }



    Path cached = null;
    boolean done = false;
    @Override
    public boolean hasNext() {
        if (done) {
            return false;
        }
        if (cached == null) {
            cached = getNext();
        }
        if (cached == null) {
            done = true;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Path next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Path toReturn = cached;
        cached = null;
        return toReturn;
    }

    private Path exploration;


    Set<List<Vertex>> seen = new HashSet<>();

    private Path getNext() {
        if (exploration.head() == b) {
            chosen[exploration.length() - 2] += 1;
            exploration.removeHead();
        }
        while (exploration.head() != b) {
            int index = exploration.length() - 1;
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
                if (!exploration.contains(neighbour)) {
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
                chosen[index] = 0;
                chosen[index - 1] += 1;
            }
        }
        seen.add(exploration.getPath());
        return exploration;
    }


}
