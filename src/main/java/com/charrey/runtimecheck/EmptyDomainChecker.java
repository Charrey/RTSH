package com.charrey.runtimecheck;

import com.charrey.algorithms.UtilityData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain checker class that prunes the search space if all target graph vertices in some source graph vertex' domain
 * have been used up by other matchings.
 */
public class EmptyDomainChecker extends DomainChecker {

    private final Integer[][] reverseDomain;
    @NotNull
    private final Set<Integer>[] domain;

    @Override
    public DomainChecker copy() {
        return new EmptyDomainChecker(this);
    }

    @SuppressWarnings("unchecked")
    private EmptyDomainChecker(EmptyDomainChecker copyFrom) {
        reverseDomain = new Integer[copyFrom.reverseDomain.length][];
        for (int i = 0; i < copyFrom.reverseDomain.length; i++) {
            reverseDomain[i] = copyFrom.reverseDomain[i].clone();
        }
        domain = (Set<Integer>[]) Array.newInstance(Set.class, copyFrom.domain.length);
        for (int i = 0; i < copyFrom.domain.length; i++) {
            domain[i] = new HashSet<>(copyFrom.domain[i]);
        }
    }


    /**
     * Instantiates a new EmptyDomainChecker
     *
     * @param data                          utility data (for cached computation)
     * @param initialNeighbourHoodFiltering whether domain filtering based on neighbourhoods should be performed.
     * @param initialGlobalAllDifferent     whether alldifferent needs to be applied initially to reduce domain sizes.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public EmptyDomainChecker(@NotNull UtilityData data, boolean initialNeighbourHoodFiltering, boolean initialGlobalAllDifferent, String name) {
        this.reverseDomain = data.getReverseCompatibility(initialNeighbourHoodFiltering, initialGlobalAllDifferent, name);
        this.domain = (Set[]) Arrays.stream(data.getCompatibility(initialNeighbourHoodFiltering, initialGlobalAllDifferent, name)).map(x -> new HashSet(Arrays.asList(x))).toArray(Set[]::new);
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int v) {
        afterRelease(v);
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int v) {
        afterRelease(v);
    }


    @Override
    public void beforeOccupyVertex(int verticesPlaced, int v) throws DomainCheckerException {
        afterOccupy(verticesPlaced, v);
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int v) throws DomainCheckerException {
        afterOccupy(verticesPlaced, v);
    }


    private void afterRelease(int v) {
        Integer[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0; i--) {
            assert !domain[candidates[i]].contains(v);
            domain[candidates[i]].add(v);
        }
    }

    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int v) {
        Integer[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0; i--) {
            domain[candidates[i]].remove(v);
        }
    }

    private void afterOccupy(int verticesPlaced, int v) throws DomainCheckerException {
        Integer[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0; i--) {
            domain[candidates[i]].remove(v);
        }
        if (Arrays.asList(domain).subList(verticesPlaced, domain.length).stream().anyMatch(Set::isEmpty)) {
            for (int i = candidates.length - 1; i >= 0; i--) {
                domain[candidates[i]].add(v);
            }
            throw new DomainCheckerException("EmptyDomain constraint failed after occupying " + v);
        }
    }

    @Override
    public boolean isUnfruitful(int verticesPlaced) {
        return Arrays.asList(domain).subList(verticesPlaced, domain.length).stream().anyMatch(Set::isEmpty);
    }


    @NotNull
    @Override
    public String toString() {
        return "EmptyDomainChecker{" + "domain=" + Arrays.toString(domain) + '}';
    }
}
