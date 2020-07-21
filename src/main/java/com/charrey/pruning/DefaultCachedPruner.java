package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultCachedPruner extends Pruner {

    protected final List<TIntSet> domain;

    private final List<TIntList> reverseDomain;

    private final TIntSet[] previousDomain;


    DefaultCachedPruner(DefaultCachedPruner copyOf) {
        super(copyOf.filter, copyOf.sourceGraph, copyOf.targetGraph, copyOf.occupation);
        this.domain = new ArrayList<>(sourceGraph.vertexSet().size());
        this.reverseDomain = new ArrayList<>(targetGraph.vertexSet().size());
        this.previousDomain = (TIntSet[]) Array.newInstance(TIntSet.class, sourceGraph.vertexSet().size());
        for (int i = 0; i < copyOf.reverseDomain.size(); i++) {
            reverseDomain.add(new TIntArrayList(copyOf.reverseDomain.get(i)));
        }
        for (int i = 0; i < copyOf.domain.size(); i++) {
            domain.add(new TIntHashSet(copyOf.domain.get(i)));
        }
        for (int i = 0; i < copyOf.previousDomain.length; i++) {
            previousDomain[i] = copyOf.previousDomain[i] == null ? null : new TIntHashSet(copyOf.previousDomain[i]);
        }
    }

    DefaultCachedPruner(FilteringSettings filter, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation, boolean calcDomains) {
        super(filter, sourceGraph, targetGraph, occupation);
        this.domain = new ArrayList<>(sourceGraph.vertexSet().size());
        this.reverseDomain = new ArrayList<>(targetGraph.vertexSet().size());
        this.previousDomain = (TIntSet[]) Array.newInstance(TIntSet.class, sourceGraph.vertexSet().size());
        if (calcDomains) {
            while (domain.size() < sourceGraph.vertexSet().size()) {
                domain.add(new TIntHashSet());
            }
            while (reverseDomain.size() < targetGraph.vertexSet().size()) {
                reverseDomain.add(new TIntArrayList());
            }
            sourceGraph.vertexSet().stream().sorted().forEach(sourceV -> targetGraph.vertexSet().forEach(targetV -> {
                if (filter.filter(sourceGraph, targetGraph, sourceV, targetV, occupation)) {
                    domain.get(sourceV).add(targetV);
                    reverseDomain.get(targetV).add(sourceV);
                }
            }));
        }
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int occupied, PartialMatching partialMatching) throws DomainCheckerException {
        beforeOccupyVertex(verticesPlaced, occupied);
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int v, PartialMatching partialMatching) throws DomainCheckerException {
        TIntList candidates2 = reverseDomain.get(v);
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(v, candidates2, sourceVertexData);
        if (isUnfruitfulCached(verticesPlaced)) {
            for (int i = candidates2.size() - 1; i >= 0 && candidates2.get(i) > sourceVertexData; i--) {
                domain.get(candidates2.get(i)).add(v);
            }
            throw new DomainCheckerException("Pruner kicked in after occupying routing vertex " + v);
        }
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int v) {
        TIntList sourceGraphCandidates = reverseDomain.get(v);
        for (int i = sourceGraphCandidates.size() - 1; i >= 0 && sourceGraphCandidates.get(i) > verticesPlaced; i--) {
            assert !domain.get(sourceGraphCandidates.get(i)).contains(v);
            domain.get(sourceGraphCandidates.get(i)).add(v);
        }
        domain.set(verticesPlaced, previousDomain[verticesPlaced]);
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int v) {
        TIntList sourceGraphCandidates = reverseDomain.get(v);
        for (int i = sourceGraphCandidates.size() - 1; i >= 0 && sourceGraphCandidates.get(i) >= verticesPlaced; i--) {
            domain.get(sourceGraphCandidates.get(i)).add(v);
        }
    }

    private void beforeOccupyVertex(int verticesPlaced, int v) throws DomainCheckerException {
        TIntList sourceGraphCandidates = reverseDomain.get(v);
        int sourceVertexData = verticesPlaced - 1;
        previousDomain[sourceVertexData] = new TIntHashSet(domain.get(sourceVertexData));
        domain.get(sourceVertexData).clear();
        domain.get(sourceVertexData).add(v);
        removeFromDomains(v, sourceGraphCandidates, sourceVertexData);
        if (isUnfruitfulCached(verticesPlaced)) {
            for (int i = sourceGraphCandidates.size() - 1; i >= 0 && sourceGraphCandidates.get(i) > sourceVertexData; i--) {
                domain.get(sourceGraphCandidates.get(i)).add(v);
            }
            domain.set(sourceVertexData, previousDomain[sourceVertexData]);
            throw new DomainCheckerException("Pruner kicked in after occupying vertex " + v);
        }
    }

    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int v) {
        TIntList candidates2 = reverseDomain.get(v);
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(v, candidates2, sourceVertexData);
    }

    private void removeFromDomains(int placedTarget, TIntList sourcegraphCandidates, int sourceVertexData) {
        for (int i = sourcegraphCandidates.size() - 1; i >= 0 && sourcegraphCandidates.get(i) > sourceVertexData; i--) {
            assert domain.get(sourcegraphCandidates.get(i)).contains(placedTarget);
            domain.get(sourcegraphCandidates.get(i)).remove(placedTarget);
        }
    }


    @Override
    public void checkPartial(PartialMatching partialMatching) {
        throw new UnsupportedOperationException("Cached pruner cannot check partial.");
    }
}
