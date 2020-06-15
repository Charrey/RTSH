package com.charrey.pathiterators.controlpoint;

import com.charrey.occupation.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.util.GraphUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ControlPointVertexSelector implements Iterator<Vertex> {

    @NotNull
    private final Occupation occupation;
    @Nullable
    private final List<Vertex> vertices;
    @NotNull
    private final Set<Integer> localOccupation;
    private int indexTried = -1;


    ControlPointVertexSelector(@NotNull MyGraph graph,
                               @NotNull Occupation occupation,
                               @NotNull Set<Integer> initialLocalOccupation,
                               @NotNull Vertex from,
                               @NotNull Vertex to) {
        this.occupation = occupation;
        this.localOccupation = initialLocalOccupation;
        Random random = new Random(1 + 3*graph.hashCode() + 5*occupation.hashCode() + 7*initialLocalOccupation.hashCode() + 11*from.data() + 13*to.data());
        vertices = GraphUtil.randomVertexOrder(graph, random);
        Path path = ControlPointIterator.filteredShortestPath(graph, occupation, initialLocalOccupation, from, to);
        if (path == null) {
            readyToDeliver = true;
            nextToReturn = null;
        }
    }

    @Nullable
    private Vertex nextToReturn = null;
    private boolean readyToDeliver = false;


    @Override
    public boolean hasNext() {
        if (!readyToDeliver) {
            nextToReturn = iterate();
            readyToDeliver = true;
        }
        return nextToReturn != null;
    }

    @Nullable
    private Vertex iterate() {
        assert vertices != null;
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

    private boolean isSuitable(@NotNull Vertex vertex) {
        return !occupation.isOccupied(vertex) && !localOccupation.contains(vertex.data());
    }

    @Nullable
    @Override
    public Vertex next() {
        if (!readyToDeliver) {
            nextToReturn = iterate();
        }
        readyToDeliver = false;
        return nextToReturn;
    }


}
