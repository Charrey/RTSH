package com.charrey.pruning;

import com.charrey.algorithms.UtilityData;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;

/**
 * Domain checker class that prunes the search space if all target graph vertices in some source graph vertex' domain
 * have been used up by other matchings.
 */
public class ZeroDomainPruner extends Pruner {

    private final int[][] reverseDomain;
    @NotNull
    private final TIntSet[] domain;

    @SuppressWarnings("unchecked")
    private ZeroDomainPruner(ZeroDomainPruner copyFrom) {
        reverseDomain = new int[copyFrom.reverseDomain.length][];
        for (int i = 0; i < copyFrom.reverseDomain.length; i++) {
            reverseDomain[i] = copyFrom.reverseDomain[i].clone();
        }
        domain = (TIntSet[]) Array.newInstance(Set.class, copyFrom.domain.length);
        for (int i = 0; i < copyFrom.domain.length; i++) {
            domain[i] = new TIntHashSet(copyFrom.domain[i]);
        }
    }

    /**
     * Instantiates a new ZeroDomainPruner
     *
     * @param data utility data (for cached computation)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ZeroDomainPruner(@NotNull UtilityData data, FilteringSettings filteringSettings, String name, boolean cached) {
        this.reverseDomain = data.getReverseCompatibility(filteringSettings, name);
        this.domain = Arrays.stream(data.getCompatibility(filteringSettings, name)).map(TIntHashSet::new).toArray(TIntSet[]::new);
    }

    @Override
    public Pruner copy() {
        return new ZeroDomainPruner(this);
    }

    @Override
    public int serialized() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        return getClass().equals(o.getClass());
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
        int[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0; i--) {
            assert !domain[candidates[i]].contains(v);
            domain[candidates[i]].add(v);
        }
    }

    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int v) {
        int[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0; i--) {
            domain[candidates[i]].remove(v);
        }
    }

    private void afterOccupy(int verticesPlaced, int v) throws DomainCheckerException {
        int[] candidates = reverseDomain[v];
        for (int i = candidates.length - 1; i >= 0; i--) {
            domain[candidates[i]].remove(v);
        }
        if (Arrays.asList(domain).subList(verticesPlaced, domain.length).stream().anyMatch(TIntSet::isEmpty)) {
            for (int i = candidates.length - 1; i >= 0; i--) {
                domain[candidates[i]].add(v);
            }
            throw new DomainCheckerException("EmptyDomain constraint failed after occupying " + v);
        }
    }

    @Override
    public boolean isUnfruitful(int verticesPlaced) {
        return Arrays.asList(domain).subList(verticesPlaced, domain.length).stream().anyMatch(TIntSet::isEmpty);
    }


    @NotNull
    @Override
    public String toString() {
        return "ZeroDomainPruner{" + "domain=" + Arrays.toString(domain) + '}';
    }
}
