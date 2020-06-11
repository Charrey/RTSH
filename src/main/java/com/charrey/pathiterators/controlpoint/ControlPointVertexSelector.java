package com.charrey.pathiterators.controlpoint;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.util.GraphUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ControlPointVertexSelector implements Iterator<Vertex> {

    private final Occupation occupation;
    private final List<Vertex> vertices;
    private final Set<Integer> local;
    int indexTried = -1;


    public ControlPointVertexSelector(MyGraph graph,
                                      Occupation occupation,
                                      Set<Integer> local,
                                      Vertex from,
                                      Vertex to) {
        this.occupation = occupation;
        this.local = local;
        vertices = GraphUtil.randomVertexOrder(graph);
        Path path = ControlPointIterator.filteredShortestPath(graph, occupation, local, from, to);
        if (path == null) {
            readyToDeliver = true;
            nextToReturn = null;
        }
    }

    Vertex nextToReturn = null;
    boolean readyToDeliver = false;


    @Override
    public boolean hasNext() {
        if (!readyToDeliver) {
            nextToReturn = iterate();
            readyToDeliver = true;
        }
        return nextToReturn != null;
    }

    private Vertex iterate() {
        while (true) {
            int newIndex = indexTried + 1;
            if (newIndex >= vertices.size()) {
                return null;
            }
            Vertex vertex = vertices.get(newIndex);
            indexTried = newIndex;
            if (isSuitable(vertex)) {
                return vertex;
            }
            indexTried = newIndex;
        }
    }

    private boolean isSuitable(Vertex vertex) {
        return !occupation.isOccupied(vertex) && !local.contains(vertex.data());
    }

    @Override
    public Vertex next() {
        if (!readyToDeliver) {
            nextToReturn = iterate();
        }
        readyToDeliver = false;
        return nextToReturn;
    }


}
