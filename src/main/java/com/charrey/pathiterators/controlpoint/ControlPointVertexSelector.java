package com.charrey.pathiterators.controlpoint;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.AbstractOccupation;
import com.charrey.util.GraphUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ControlPointVertexSelector implements Iterator<Integer> {

    @NotNull
    private final AbstractOccupation occupation;
    @Nullable
    private final List<Integer> vertices;
    @NotNull
    private final Set<Integer> localOccupation;
    private int indexTried = -1;


    ControlPointVertexSelector(@NotNull MyGraph graph,
                               @NotNull AbstractOccupation occupation,
                               @NotNull Set<Integer> initialLocalOccupation,
                               int from,
                               int to,
                               boolean refuseLongerPaths,
                               int tail) {
        this.occupation = occupation;
        this.localOccupation = initialLocalOccupation;
        Random random = new Random(1 + 3*graph.hashCode() + 5*occupation.hashCode() + 7*initialLocalOccupation.hashCode() + 11*from + 13*to);
        vertices = GraphUtil.randomVertexOrder(graph, random);
        Path path = ControlPointIterator.filteredShortestPath(graph, occupation, initialLocalOccupation, from, to, refuseLongerPaths, tail);
        if (path == null) {
            readyToDeliver = true;
            nextToReturn = -1;
        }
    }

    private int nextToReturn = -1;
    private boolean readyToDeliver = false;


    @Override
    public boolean hasNext() {
        if (!readyToDeliver) {
            nextToReturn = iterate();
            readyToDeliver = true;
        }
        return nextToReturn != -1;
    }

    private int iterate() {
        assert vertices != null;
        while (true) {
            int newIndex = indexTried + 1;
            if (newIndex >= vertices.size()) {
                return -1;
            }
            int vertex = vertices.get(newIndex);
            indexTried = newIndex;
            if (isSuitable(vertex)) {
                return vertex;
            }
            indexTried = newIndex;
        }
    }

    private boolean isSuitable(int vertex) {
        return !occupation.isOccupied(vertex) && !localOccupation.contains(vertex);
    }

    @Override
    public Integer next() {
        if (!readyToDeliver) {
            nextToReturn = iterate();
        }
        readyToDeliver = false;
        return nextToReturn;
    }


}
