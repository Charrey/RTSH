package com.charrey.pruning.serial;

import com.charrey.graph.MyGraph;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.ReadOnlyOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;

public abstract class DefaultSerialPruner extends Pruner {

    protected DefaultSerialPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, ReadOnlyOccupation occupation) {
        super(settings, sourceGraph, targetGraph, occupation);
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int released, PartialMatchingProvider partialMatchingProvider) {
        //ignore
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int released, PartialMatchingProvider partialMatchingProvider) {
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
    public boolean isUnfruitful(int verticesPlaced, PartialMatchingProvider partialMatchingProvider) {
        try {
            checkPartial(partialMatchingProvider);
        } catch (DomainCheckerException e) {
            return true;
        }
        return false;
    }
}
