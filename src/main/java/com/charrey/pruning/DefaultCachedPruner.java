package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;

public abstract class DefaultCachedPruner extends Pruner {

    DefaultCachedPruner(FilteringSettings filter, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation) {
        super(filter, sourceGraph, targetGraph, occupation);
    }

    public abstract void beforeOccupyVertex(int verticesPlaced, int occupied) throws DomainCheckerException;

    public abstract void afterOccupyEdge(int verticesPlaced, int occupied) throws DomainCheckerException;

    public abstract void afterOccupyEdgeWithoutCheck(int verticesPlaced, int occupied);


    @Override
    public void beforeOccupyVertex(int verticesPlaced, int occupied, PartialMatching partialMatching) throws DomainCheckerException {
        beforeOccupyVertex(verticesPlaced, occupied);
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int occupied, PartialMatching partialMatching) throws DomainCheckerException {
        afterOccupyEdge(verticesPlaced, occupied);
    }


    @Override
    public void checkPartial(PartialMatching partialMatching) {
        throw new UnsupportedOperationException("Cached pruner cannot check partial.");
    }
}
