package com.charrey.pruning.cached;

import com.charrey.graph.MyGraph;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;
import com.charrey.settings.pruning.domainfilter.LabelDegreeFiltering;
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
        super(copyOf.settings, copyOf.sourceGraph, copyOf.targetGraph, copyOf.occupation);
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

    DefaultCachedPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation) {
        super(settings, sourceGraph, targetGraph, occupation);
        this.domain = new ArrayList<>(sourceGraph.vertexSet().size());
        this.reverseDomain = new ArrayList<>(targetGraph.vertexSet().size());
        this.previousDomain = (TIntSet[]) Array.newInstance(TIntSet.class, sourceGraph.vertexSet().size());
        while (domain.size() < sourceGraph.vertexSet().size()) {
            domain.add(new TIntHashSet());
        }
        while (reverseDomain.size() < targetGraph.vertexSet().size()) {
            reverseDomain.add(new TIntArrayList());
        }
        sourceGraph.vertexSet().stream().sorted().forEach(sourceV -> targetGraph.vertexSet().forEach(targetV -> {
            if (new LabelDegreeFiltering().filter(sourceGraph, targetGraph, sourceV, targetV, occupation)) {
                domain.get(sourceV).add(targetV);
                reverseDomain.get(targetV).add(sourceV);
            }
        }));
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int targetVertex, PartialMatchingProvider partialMatchingProvider) throws DomainCheckerException {
        TIntList sourceGraphCandidates = reverseDomain.get(targetVertex);
        int sourceVertexData = verticesPlaced - 1;
        previousDomain[sourceVertexData] = new TIntHashSet(domain.get(sourceVertexData));
        domain.get(sourceVertexData).clear();
        domain.get(sourceVertexData).add(targetVertex);
        removeFromDomains(targetVertex, sourceGraphCandidates, sourceVertexData);
        if (isUnfruitful(verticesPlaced, partialMatchingProvider)) {
            for (int i = sourceGraphCandidates.size() - 1; i >= 0 && sourceGraphCandidates.get(i) > sourceVertexData; i--) {
                domain.get(sourceGraphCandidates.get(i)).add(targetVertex);
            }
            domain.set(sourceVertexData, previousDomain[sourceVertexData]);
            throw new DomainCheckerException(() -> "Pruner kicked in after occupying vertex " + targetVertex);
        }
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int newlyOccupied, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        TIntList candidates = reverseDomain.get(newlyOccupied);
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(newlyOccupied, candidates, sourceVertexData);
        if (isUnfruitful(verticesPlaced, partialMatching)) {
            for (int i = candidates.size() - 1; i >= 0 && candidates.get(i) > sourceVertexData; i--) {
                domain.get(candidates.get(i)).add(newlyOccupied);
            }
            throw new DomainCheckerException(() -> "Pruner kicked in after occupying routing vertex " + newlyOccupied);
        }
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int v, PartialMatchingProvider partialMatchingProvider) {
        TIntList sourceGraphCandidates = reverseDomain.get(v);
        for (int i = sourceGraphCandidates.size() - 1; i >= 0 && sourceGraphCandidates.get(i) > verticesPlaced; i--) {
            if (domain.get(sourceGraphCandidates.get(i)).contains(v)) {
                throw new IllegalStateException();
            }
            domain.get(sourceGraphCandidates.get(i)).add(v);
        }
        domain.set(verticesPlaced, previousDomain[verticesPlaced]);
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int v, PartialMatchingProvider partialMatchingProvider) {
        TIntList sourceGraphCandidates = reverseDomain.get(v);
        for (int i = sourceGraphCandidates.size() - 1; i >= 0 && sourceGraphCandidates.get(i) >= verticesPlaced; i--) {
            domain.get(sourceGraphCandidates.get(i)).add(v);
        }
    }



    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int v) {
        TIntList candidates2 = reverseDomain.get(v);
        removeFromDomains(v, candidates2, verticesPlaced - 1);
    }

    private void removeFromDomains(int placedTarget, TIntList sourcegraphCandidates, int sourceVertexData) {
        for (int i = sourcegraphCandidates.size() - 1; i >= 0 && sourcegraphCandidates.get(i) > sourceVertexData; i--) {
            domain.get(sourcegraphCandidates.get(i)).remove(placedTarget);
        }
    }


    @Override
    public void checkPartial(PartialMatchingProvider partialMatching) {
        throw new UnsupportedOperationException("Cached pruner cannot check partial.");
    }
}
