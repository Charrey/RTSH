package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;

public abstract class DefaultSerialPruner extends Pruner {

    DefaultSerialPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation) {
        super(settings, sourceGraph, targetGraph, occupation);
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int released) {
        //ignore
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int released) {
        //ignore
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int occupied, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        checkPartial(partialMatching);
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int occupied, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        checkPartial(partialMatching);
    }

    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int occupied) {
        //ignore
    }

    @Override
    public boolean isUnfruitfulCached(int verticesPlaced) {
        throw new UnsupportedOperationException("Cached operations not available in SerialPruner.");
    }
}
