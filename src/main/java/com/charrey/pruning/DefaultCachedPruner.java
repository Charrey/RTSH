package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;

public abstract class DefaultCachedPruner extends Pruner {

    protected final ArrayList<TIntSet> domain;

    DefaultCachedPruner(FilteringSettings filter, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation, boolean calcDomains) {
        super(filter, sourceGraph, targetGraph, occupation);
        this.domain = new ArrayList<>(sourceGraph.vertexSet().size());
        if (calcDomains) {
            sourceGraph.vertexSet().stream().sorted().forEach(sourceV -> {
                domain.add(new TIntHashSet());
                targetGraph.vertexSet().forEach(targetV -> {
                    if (filter.filter(sourceGraph, targetGraph, sourceV, targetV, occupation)) {
                        domain.get(sourceV).add(targetV);
                    }
                });
            });
        }
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
