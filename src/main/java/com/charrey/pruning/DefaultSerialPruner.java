package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;

public abstract class DefaultSerialPruner extends Pruner {

    DefaultSerialPruner(FilteringSettings filter, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation) {
        super(filter, sourceGraph, targetGraph, occupation);
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
    public void beforeOccupyVertex(int verticesPlaced, int occupied, PartialMatching partialMatching) throws DomainCheckerException {
        checkPartial(partialMatching);
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int occupied, PartialMatching partialMatching) throws DomainCheckerException {
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
