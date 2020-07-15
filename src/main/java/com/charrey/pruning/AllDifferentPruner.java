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

//    @NotNull
//    private final List<TIntSet> domain;

    private final List<TIntList> reverseDomain2;

    @NotNull
    private final TIntSet[] previousDomain;

    private AllDifferentPruner(AllDifferentPruner copyOf) {
        super(copyOf.filter, copyOf.sourceGraph, copyOf.targetGraph, copyOf.occupation, false);
        reverseDomain2 = new ArrayList<>(copyOf.reverseDomain2.size());
        for (int i = 0; i < copyOf.reverseDomain2.size(); i++) {
            reverseDomain2.add(new TIntArrayList(copyOf.reverseDomain2.get(i)));
        }
        this.allDifferent = copyOf.allDifferent;
        for (int i = 0; i < copyOf.domain.size(); i++) {
            domain.add(new TIntHashSet(copyOf.domain.get(i)));
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
        super(filteringSettings, data.getPatternGraph(), data.getTargetGraph(), occupation, true);
        this.allDifferent = new AllDifferent();
        previousDomain = (TIntSet[]) Array.newInstance(TIntSet.class, domain.size());
        reverseDomain2 = Arrays.stream(data.getReverseCompatibility(filteringSettings, name)).map(TIntArrayList::new).collect(Collectors.toList());
    }

    private void popVertex(int data) {
        TIntSet popped = previousDomain[data];
        domain.set(data, popped);
    }

    private void pushVertex(int data) {
        previousDomain[data] = new TIntHashSet(domain.get(data));
    }

    @Override
    public Pruner copy() {
        return new AllDifferentPruner(this);
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int v) {
        TIntList candidates2 = reverseDomain2.get(v);
        for (int i = candidates2.size() - 1; i >= 0 && candidates2.get(i) > verticesPlaced; i--) {
            assert !domain.get(candidates2.get(i)).contains(v);
            domain.get(candidates2.get(i)).add(v);
        }
        TIntSet popped = previousDomain[verticesPlaced];
        domain.get(verticesPlaced).addAll(popped);
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int v) {
        TIntList candidates2 = reverseDomain2.get(v);
        for (int i = candidates2.size() - 1; i >= 0 && candidates2.get(i) >= verticesPlaced; i--) {
            domain.get(candidates2.get(i)).add(v);
        }
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int v) throws DomainCheckerException {
        TIntList candidates2 = reverseDomain2.get(v);
        int sourceVertexData = verticesPlaced - 1;
        pushVertex(sourceVertexData);
        domain.get(sourceVertexData).clear();
        domain.get(sourceVertexData).add(v);
        removeFromDomains(v, candidates2, sourceVertexData);
        if (isUnfruitfulCached(verticesPlaced)) {
            for (int i = candidates2.size() - 1; i >= 0 && candidates2.get(i) > sourceVertexData; i--) {
                domain.get(candidates2.get(i)).add(v);
            }
            popVertex(sourceVertexData);
            throw new DomainCheckerException("AllDifferent constraint failed after occupying vertex " + v);
        }
    }


    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int v) {
        TIntList candidates2 = reverseDomain2.get(v);
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(v, candidates2, sourceVertexData);
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int v) throws DomainCheckerException {
        TIntList candidates2 = reverseDomain2.get(v);
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(v, candidates2, sourceVertexData);


        if (isUnfruitfulCached(verticesPlaced)) {
            for (int i = candidates2.size() - 1; i >= 0 && candidates2.get(i) > sourceVertexData; i--) {
                domain.get(candidates2.get(i)).add(v);
            }
            throw new DomainCheckerException("AllDifferent constraint failed after occupying routing vertex " + v);
        }
    }


    private void removeFromDomains(int placedTarget, TIntList sourcegraphCandidates, int sourceVertexData) {
        for (int i = sourcegraphCandidates.size() - 1; i >= 0 && sourcegraphCandidates.get(i) > sourceVertexData; i--) {
            assert domain.get(sourcegraphCandidates.get(i)).contains(placedTarget);
            domain.get(sourcegraphCandidates.get(i)).remove(placedTarget);
        }
    }


    @Override
    public boolean isUnfruitfulCached(int verticesPlaced) {
        return domain.stream().anyMatch(TIntSet::isEmpty) || !allDifferent.get(domain);
    }


    @NotNull
    @Override
    public String toString() {
        TIntList[] domainString = new TIntList[domain.size()];
        for (int i = 0; i < domainString.length; i++) {
            domainString[i] = new TIntArrayList(domain.get(i));
            domainString[i].sort();
        }
        return "AllDifferentPruner{domain=" + Arrays.toString(domainString) +
                '}';
    }
}
