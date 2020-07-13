package com.charrey.matching;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class VertexCandidateIterator implements Iterator<Integer>, Iterable<Integer> {

    private final MyGraph sourceGraph;
    private final MyGraph targetGraph;
    private final int vertex;
    private final FilteringSettings neighbourhoodFiltering;
    private final GlobalOccupation occupation;

    private Iterator<Integer> innerIterator;
    private int nextToReturn = -1;

    public VertexCandidateIterator(MyGraph sourceGraph, MyGraph targetGraph, int vertex, FilteringSettings neighbourhoodFiltering, GlobalOccupation occupation) {
        this.sourceGraph = sourceGraph;
        this.targetGraph = targetGraph;
        this.vertex = vertex;
        this.neighbourhoodFiltering = neighbourhoodFiltering;
        this.innerIterator = getInnerIterator();
        this.occupation = occupation;
    }

    private Iterator<Integer> getInnerIterator() {
        return targetGraph.vertexSet().stream().filter(x ->
                neighbourhoodFiltering.filter(sourceGraph, targetGraph, vertex, x, occupation)).iterator();
    }

    public void reset() {
        this.nextToReturn = -1;
        this.innerIterator = getInnerIterator();
    }

    private void prepareNextToReturn() {
        if (!innerIterator.hasNext()) {
            nextToReturn = -2;
        } else {
            nextToReturn = innerIterator.next();
        }
    }

    @Override
    public boolean hasNext() {
        if (nextToReturn == -1) {
            prepareNextToReturn();
        }
        return nextToReturn != -2;
    }


    @Override
    public Integer next() {
        if (nextToReturn == -1) {
            prepareNextToReturn();
        }
        if (nextToReturn == -2) {
            throw new NoSuchElementException();
        } else {
            int toReturn = nextToReturn;
            nextToReturn = -1;
            return toReturn;
        }
    }


    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return this;
    }


}
