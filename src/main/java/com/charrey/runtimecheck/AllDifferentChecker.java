package com.charrey.runtimecheck;

import com.charrey.algorithms.AllDifferent;
import com.charrey.algorithms.UtilityData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Domainchecker that prematurely stops the search when an AllDifferent constraint fails, proving the current search
 * path is unfruitful.
 */
public class AllDifferentChecker extends DomainChecker {

    @NotNull
    private final AllDifferent allDifferent;
    @NotNull
    private final Set<Integer>[] domain;
    private final Integer[][] reverseDomain;
    @NotNull
    private final Deque<Set<Integer>>[] vertexState;

    @SuppressWarnings("unchecked")
    private AllDifferentChecker(AllDifferentChecker copyOf) {
        super();
        reverseDomain = new Integer[copyOf.reverseDomain.length][];
        for (int i = 0; i < copyOf.reverseDomain.length; i++) {
            reverseDomain[i] = copyOf.reverseDomain[i].clone();
        }
        this.allDifferent = copyOf.allDifferent;
        domain = (Set<Integer>[]) Array.newInstance(Set.class, copyOf.domain.length);
        for (int i = 0; i < copyOf.domain.length; i++) {
            domain[i] = new HashSet<>(copyOf.domain[i]);
        }
        vertexState = (Deque<Set<Integer>>[]) Array.newInstance(Deque.class, copyOf.vertexState.length);
        for (int i = 0; i < copyOf.vertexState.length; i++) {
            vertexState[i] = copyOf.vertexState[i].stream().map(HashSet::new).distinct().collect(Collectors.toCollection(LinkedList::new));
        }
    }

    private void pushVertex(int data) {
        vertexState[data].push(new HashSet<>(domain[data]));
    }

    private void popVertex(int data) {
        domain[data] = vertexState[data].pop();
    }

    @Override
    public DomainChecker copy() {
        return new AllDifferentChecker(this);
    }


    /**
     * Instantiates a new AllDifferentChecker
     *
     * @param data                          utility data (for cached computation)
     * @param initialNeighbourhoodFiltering whether domain filtering based on neighbourhoods should be performed.
     * @param initialGlobalAllDifferent     whether alldifferent needs to be applied initially to reduce domain sizes.
     */
    @SuppressWarnings({"unchecked"})
    public AllDifferentChecker(@NotNull UtilityData data, boolean initialNeighbourhoodFiltering, boolean initialGlobalAllDifferent) {
        this.allDifferent = new AllDifferent();
        reverseDomain = data.getReverseCompatibility(initialNeighbourhoodFiltering, initialGlobalAllDifferent);
        this.domain = Arrays.stream(data.getCompatibility(initialNeighbourhoodFiltering, initialGlobalAllDifferent)).map(x -> new HashSet<>(Arrays.asList(x))).toArray(value -> (Set<Integer>[]) new Set[value]);
        vertexState = (Deque<Set<Integer>>[]) Array.newInstance(Deque.class, domain.length);
        for (int i = 0; i < domain.length; i++) {
            vertexState[i] = new LinkedList<>();
        }
    }


    @Override
    public void afterReleaseVertex(int verticesPlaced, int v) {
        Integer[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0 && candidates[i] > verticesPlaced; i--) {
            assert !domain[candidates[i]].contains(v);
            domain[candidates[i]].add(v);
        }
        domain[verticesPlaced].addAll(vertexState[verticesPlaced].pop());
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int v) {
        Integer[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0 && candidates[i] >= verticesPlaced; i--) {
            domain[candidates[i]].add(v);
        }
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int v) throws DomainCheckerException {
        Integer[] candidates = reverseDomain[v];
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
        Integer[] candidates = reverseDomain[v];
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(v, candidates, sourceVertexData);
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int v) throws DomainCheckerException {
        Integer[] candidates = reverseDomain[v];
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(v, candidates, sourceVertexData);
        if (isUnfruitful(verticesPlaced)) {
            for (int i = candidates.length - 1; i >= 0 && candidates[i] > sourceVertexData; i--) {
                domain[candidates[i]].add(v);
            }
            throw new DomainCheckerException("AllDifferent constraint failed after occupying routing vertex " + v);
        }
    }


    private void removeFromDomains(int placedTarget, Integer[] sourcegraphCandidates, int sourceVertexData) {
        for (int i = sourcegraphCandidates.length - 1; i >= 0 && sourcegraphCandidates[i] > sourceVertexData; i--) {
            assert domain[sourcegraphCandidates[i]].contains(placedTarget);
            domain[sourcegraphCandidates[i]].remove(placedTarget);
        }
    }


    @Override
    public boolean isUnfruitful(int verticesPlaced) {
        return Arrays.stream(domain).anyMatch(Set::isEmpty) || !allDifferent.get(Arrays.asList(domain));
    }


    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public String toString() {
        List[] domainString = new List[domain.length];
        for (int i = 0; i < domainString.length; i++) {
            domainString[i] = new ArrayList<>(domain[i]);
            domainString[i].sort(Comparator.comparingInt(o -> (Integer) o));
        }
        return "AllDifferentChecker{domain=" + Arrays.toString(domainString) +
                '}';
    }
}
