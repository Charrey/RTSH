package com.charrey.matching.candidate;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;

import java.util.Iterator;

public class IndexIterator extends VertexCandidateIterator {


    private Iterator<Integer> innerIterator;

    public IndexIterator(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, Settings settings, GlobalOccupation occupation) {
        super(sourceGraph, targetGraph, settings, occupation, sourceGraphVertex);
        reset();
    }

    @Override
    public void doReset() {
        this.nextToReturn = ABSENT;
        this.innerIterator = getInnerIterator();
    }

    @Override
    protected void prepareNextToReturn() {
        if (!innerIterator.hasNext()) {
            nextToReturn = EXHAUSTED;
        } else {
            nextToReturn = innerIterator.next();
        }
    }
}
