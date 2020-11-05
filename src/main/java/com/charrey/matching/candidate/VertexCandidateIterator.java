package com.charrey.matching.candidate;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;
import com.charrey.settings.pruning.domainfilter.UnmatchedDegreesFiltering;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class VertexCandidateIterator implements Iterator<Integer> {

    static final int ABSENT = -1;
    static final int EXHAUSTED = -2;
    protected final MyGraph sourceGraph;
    protected final MyGraph targetGraph;
    protected final GlobalOccupation occupation;
    protected final int sourceGraphVertex;
    private final int limit;
    int nextToReturn = -1;
    private int counter = 0;


    VertexCandidateIterator(MyGraph sourceGraph, MyGraph targetGraph, Settings settings, GlobalOccupation occupation, int sourceGraphVertex) {
        this.sourceGraph = sourceGraph;
        this.targetGraph = targetGraph;
        this.occupation = occupation;
        this.sourceGraphVertex = sourceGraphVertex;
        this.limit = settings.getVertexLimit();
    }

    Iterator<Integer> getInnerIterator() {
        return targetGraph.vertexSet().stream().filter(x ->
                new UnmatchedDegreesFiltering().filter(sourceGraph, targetGraph, sourceGraphVertex, x, occupation) && !occupation.isOccupied(x))
                .iterator();
    }

    public abstract void doReset();

    public void reset() {
        counter = 0;
        doReset();
    }

    @Override
    public Integer next() {
        if (counter >= limit) {
            throw new NoSuchElementException();
        }
        if (nextToReturn == ABSENT) {
            prepareNextToReturn();
        }
        if (nextToReturn == EXHAUSTED) {
            throw new NoSuchElementException();
        } else {
            int toReturn = nextToReturn;
            nextToReturn = ABSENT;
            counter++;
            return toReturn;
        }
    }

    @Override
    public boolean hasNext() {
        if (counter >= limit) {
            return false;
        }
        while (nextToReturn == ABSENT) {
            prepareNextToReturn();
        }
        return nextToReturn != EXHAUSTED;
    }

    protected abstract void prepareNextToReturn();


}
