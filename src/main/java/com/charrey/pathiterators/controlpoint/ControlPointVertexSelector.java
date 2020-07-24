package com.charrey.pathiterators.controlpoint;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.AbstractOccupation;
import com.charrey.util.GraphUtil;
import gnu.trove.list.TIntList;
import gnu.trove.set.TIntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Class that iterates over vertex suitable as control point for the ControlPointIterator class
 */
public class ControlPointVertexSelector {

    @NotNull
    private final AbstractOccupation occupation;
    @Nullable
    private final TIntList vertices;
    @NotNull
    private final TIntSet localOccupation;
    private int indexTried = -1;


    /**
     * Creates a new instance of this object.
     *
     * @param graph                  the target graph
     * @param occupation             the global occupation in which used vertices are registered
     * @param initialLocalOccupation the local occupation in which locally used vertices are registered
     * @param from                   source vertex of the path
     * @param to                     target vertex of the path
     * @param refuseLongerPaths      whether to refuse paths that take unnecessarily many resources
     * @param tail                   end goal vertex of this path (since ControlPointIterator is recursive)
     */
    ControlPointVertexSelector(@NotNull MyGraph graph,
                               @NotNull AbstractOccupation occupation,
                               @NotNull TIntSet initialLocalOccupation,
                               int from,
                               int to,
                               boolean refuseLongerPaths,
                               int tail) {
        this.occupation = occupation;
        this.localOccupation = initialLocalOccupation;
        Random random = new Random(1 + 3L * graph.hashCode() + 5L * occupation.hashCode() + 7L * initialLocalOccupation.hashCode() + 11L * from + 13L * to);
        vertices = GraphUtil.randomVertexOrder(graph, random);
        Path path = ControlPointIterator.filteredShortestPath(graph, occupation, initialLocalOccupation, from, to, refuseLongerPaths, tail);
        if (path == null) {
            readyToDeliver = true;
            nextToReturn = -1;
        }
    }

    private int nextToReturn = -1;
    private boolean readyToDeliver = false;



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


    public Integer next() {
        if (!readyToDeliver) {
            nextToReturn = iterate();
        }
        readyToDeliver = false;
        return nextToReturn;
    }


}
