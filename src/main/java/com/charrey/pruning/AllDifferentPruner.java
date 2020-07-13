package com.charrey.pruning;

import com.charrey.algorithms.AllDifferent;
import com.charrey.algorithms.UtilityData;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Domainchecker that prematurely stops the search when an AllDifferent constraint fails, proving the current search
 * path is unfruitful.
 */
public class AllDifferentPruner extends Pruner {

    @NotNull
    private final AllDifferent allDifferent;
    @NotNull
    private final TIntSet[] domain;
    private final int[][] reverseDomain;
    @NotNull
    private final Deque<TIntSet>[] vertexState;

    @SuppressWarnings("unchecked")
    private AllDifferentPruner(AllDifferentPruner copyOf) {
        super();
        reverseDomain = new int[copyOf.reverseDomain.length][];
        for (int i = 0; i < copyOf.reverseDomain.length; i++) {
            reverseDomain[i] = copyOf.reverseDomain[i].clone();
        }
        this.allDifferent = copyOf.allDifferent;
        domain = (TIntSet[]) Array.newInstance(TIntSet.class, copyOf.domain.length);
        for (int i = 0; i < copyOf.domain.length; i++) {
            domain[i] = new TIntHashSet(copyOf.domain[i]);
        }
        vertexState = (Deque<TIntSet>[]) Array.newInstance(Deque.class, copyOf.vertexState.length);
        for (int i = 0; i < copyOf.vertexState.length; i++) {
            vertexState[i] = copyOf.vertexState[i].stream().map(TIntHashSet::new).distinct().collect(Collectors.toCollection(LinkedList::new));
        }
    }

    /**
     * Instantiates a new AllDifferentPruner
     *
     * @param data utility data (for cached computation)
     */
    @SuppressWarnings({"unchecked"})
    public AllDifferentPruner(@NotNull UtilityData data, FilteringSettings filteringSettings, String name) {
        this.allDifferent = new AllDifferent();
        reverseDomain = data.getReverseCompatibility(filteringSettings, name);
        this.domain = Arrays.stream(data.getCompatibility(filteringSettings, name)).map(TIntHashSet::new).toArray(TIntSet[]::new);
        vertexState = (Deque<TIntSet>[]) Array.newInstance(Deque.class, domain.length);
        for (int i = 0; i < domain.length; i++) {
            vertexState[i] = new LinkedList<>();
        }
    }

    private void popVertex(int data) {
        domain[data] = vertexState[data].pop();
    }

    private void pushVertex(int data) {
        vertexState[data].push(new TIntHashSet(domain[data]));
    }

    @Override
    public Pruner copy() {
        return new AllDifferentPruner(this);
    }

    @Override
    public int serialized() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        return getClass().equals(o.getClass());
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int v) {
        int[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0 && candidates[i] > verticesPlaced; i--) {
            assert !domain[candidates[i]].contains(v);
            domain[candidates[i]].add(v);
        }
        domain[verticesPlaced].addAll(vertexState[verticesPlaced].pop());
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int v) {
        int[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0 && candidates[i] >= verticesPlaced; i--) {
            domain[candidates[i]].add(v);
        }
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int v) throws DomainCheckerException {
        int[] candidates = reverseDomain[v];
        int sourceVertexData = verticesPlaced - 1;
        pushVertex(sourceVertexData);
        domain[sourceVertexData].clear();
        domain[sourceVertexData].add(v);
        removeFromDomains(v, candidates, sourceVertexData);
        if (isUnfruitful(verticesPlaced)) {
            for (int i = candidates.length - 1; i >= 0 && candidates[i] > sourceVertexData; i--) {
                domain[candidates[i]].add(v);
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
        if (isUnfruitful(verticesPlaced)) {
            for (int i = candidates.length - 1; i >= 0 && candidates[i] > sourceVertexData; i--) {
                domain[candidates[i]].add(v);
            }
            throw new DomainCheckerException("AllDifferent constraint failed after occupying routing vertex " + v);
        }
    }


    private void removeFromDomains(int placedTarget, int[] sourcegraphCandidates, int sourceVertexData) {
        for (int i = sourcegraphCandidates.length - 1; i >= 0 && sourcegraphCandidates[i] > sourceVertexData; i--) {
            assert domain[sourcegraphCandidates[i]].contains(placedTarget);
            domain[sourcegraphCandidates[i]].remove(placedTarget);
        }
    }


    @Override
    public boolean isUnfruitful(int verticesPlaced) {
        return Arrays.stream(domain).anyMatch(TIntSet::isEmpty) || !allDifferent.get(Arrays.asList(domain));
    }


    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public String toString() {
        TIntList[] domainString = new TIntList[domain.length];
        for (int i = 0; i < domainString.length; i++) {
            domainString[i] = new TIntArrayList(domain[i]);
            domainString[i].sort();
        }
        return "AllDifferentPruner{domain=" + Arrays.toString(domainString) +
                '}';
    }
}
