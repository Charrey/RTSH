package com.charrey.pruning;

import com.charrey.algorithms.AllDifferent;
import com.charrey.algorithms.UtilityData;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Domainchecker that prematurely stops the search when an AllDifferent constraint fails, proving the current search
 * path is unfruitful.
 */
public class AllDifferentPruner extends DefaultCachedPruner {

    @NotNull
    private final AllDifferent allDifferent;

    @NotNull
    private final List<TIntSet> domain2;

    private final int[][] reverseDomain;
    @NotNull
    private final TIntSet[] previousDomain;

    private AllDifferentPruner(AllDifferentPruner copyOf) {
        super(copyOf.filter, copyOf.sourceGraph, copyOf.targetGraph, copyOf.occupation);
        reverseDomain = new int[copyOf.reverseDomain.length][];
        for (int i = 0; i < copyOf.reverseDomain.length; i++) {
            reverseDomain[i] = copyOf.reverseDomain[i].clone();
        }
        this.allDifferent = copyOf.allDifferent;
        domain2 = new ArrayList<>(copyOf.sourceGraph.vertexSet().size());
        for (int i = 0; i < copyOf.domain2.size(); i++) {
            domain2.add(new TIntHashSet(copyOf.domain2.get(i)));
        }
        previousDomain = (TIntSet[]) Array.newInstance(TIntSet.class, copyOf.previousDomain.length);
        for (int i = 0; i < copyOf.previousDomain.length; i++) {
            previousDomain[i] = copyOf.previousDomain[i] == null ? null : new TIntHashSet(copyOf.previousDomain[i]);//copyOf.vertexState[i].stream().map(TIntHashSet::new).distinct().collect(Collectors.toCollection(LinkedList::new));
        }
    }

    /**
     * Instantiates a new AllDifferentPruner
     *
     * @param data utility data (for cached computation)
     */
    public AllDifferentPruner(@NotNull UtilityData data, FilteringSettings filteringSettings, String name, GlobalOccupation occupation) {
        super(filteringSettings, data.getPatternGraph(), data.getTargetGraph(), occupation);
        this.allDifferent = new AllDifferent();
        reverseDomain = data.getReverseCompatibility(filteringSettings, name);
        this.domain2 = Arrays.stream(data.getCompatibility(filteringSettings, name)).map(TIntHashSet::new).collect(Collectors.toList());
        previousDomain = (TIntSet[]) Array.newInstance(TIntSet.class, domain2.size());
    }

    private void popVertex(int data) {
        //assert vertexState[data].size() == 1;
        TIntSet popped = previousDomain[data];
        domain2.set(data, popped);
    }

    private void pushVertex(int data) {
        previousDomain[data] = new TIntHashSet(domain2.get(data));
    }

    @Override
    public Pruner copy() {
        return new AllDifferentPruner(this);
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int v) {
        int[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0 && candidates[i] > verticesPlaced; i--) {
            assert !domain2.get(candidates[i]).contains(v);
            domain2.get(candidates[i]).add(v);
        }
        TIntSet popped = previousDomain[verticesPlaced];
        domain2.get(verticesPlaced).addAll(popped);
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int v) {
        int[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0 && candidates[i] >= verticesPlaced; i--) {
            domain2.get(candidates[i]).add(v);
        }
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int v) throws DomainCheckerException {
        int[] candidates = reverseDomain[v];
        int sourceVertexData = verticesPlaced - 1;
        pushVertex(sourceVertexData);
        domain2.get(sourceVertexData).clear();
        domain2.get(sourceVertexData).add(v);
        removeFromDomains(v, candidates, sourceVertexData);
        if (isUnfruitfulCached(verticesPlaced)) {
            for (int i = candidates.length - 1; i >= 0 && candidates[i] > sourceVertexData; i--) {
                domain2.get(candidates[i]).add(v);
            }
            popVertex(sourceVertexData);
            throw new DomainCheckerException("AllDifferent constraint failed after occupying vertex " + v);
        }
    }


    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int v) {
        int[] candidates = reverseDomain[v];
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(v, candidates, sourceVertexData);
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int v) throws DomainCheckerException {
        int[] candidates = reverseDomain[v];
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(v, candidates, sourceVertexData);
        if (isUnfruitfulCached(verticesPlaced)) {
            for (int i = candidates.length - 1; i >= 0 && candidates[i] > sourceVertexData; i--) {
                domain2.get(candidates[i]).add(v);
            }
            throw new DomainCheckerException("AllDifferent constraint failed after occupying routing vertex " + v);
        }
    }


    private void removeFromDomains(int placedTarget, int[] sourcegraphCandidates, int sourceVertexData) {
        for (int i = sourcegraphCandidates.length - 1; i >= 0 && sourcegraphCandidates[i] > sourceVertexData; i--) {
            assert domain2.get(sourcegraphCandidates[i]).contains(placedTarget);
            domain2.get(sourcegraphCandidates[i]).remove(placedTarget);
        }
    }


    @Override
    public boolean isUnfruitfulCached(int verticesPlaced) {
        return domain2.stream().anyMatch(TIntSet::isEmpty) || !allDifferent.get(domain2);
    }


    @NotNull
    @Override
    public String toString() {
        TIntList[] domainString = new TIntList[domain2.size()];
        for (int i = 0; i < domainString.length; i++) {
            domainString[i] = new TIntArrayList(domain2.get(i));
            domainString[i].sort();
        }
        return "AllDifferentPruner{domain=" + Arrays.toString(domainString) +
                '}';
    }
}
