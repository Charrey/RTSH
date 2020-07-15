package com.charrey.pruning;

import com.charrey.algorithms.UtilityData;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Domain checker class that prunes the search space if all target graph vertices in some source graph vertex' domain
 * have been used up by other matchings.
 */
public class CachedZeroDomainPruner extends DefaultCachedPruner {

    @NotNull
    private final List<TIntSet> reverseDomain;
    @NotNull
    private final List<TIntSet> domain;

    private CachedZeroDomainPruner(CachedZeroDomainPruner copyFrom) {
        super(copyFrom.filter, copyFrom.sourceGraph, copyFrom.targetGraph, copyFrom.occupation);
        reverseDomain = new ArrayList<>(copyFrom.sourceGraph.vertexSet().size());
        for (int i = 0; i < copyFrom.reverseDomain.size(); i++) {
            reverseDomain.add(new TIntHashSet(copyFrom.reverseDomain.get(i)));
        }
        domain = new ArrayList<>(copyFrom.sourceGraph.vertexSet().size());
        for (int i = 0; i < copyFrom.domain.size(); i++) {
            domain.add(new TIntHashSet(copyFrom.domain.get(i)));
        }
    }

    /**
     * Instantiates a new CachedZeroDomainPruner
     *
     * @param data utility data (for cached computation)
     */
    public CachedZeroDomainPruner(@NotNull UtilityData data, FilteringSettings filteringSettings, String name, GlobalOccupation occupation) {
        super(filteringSettings, data.getPatternGraph(), data.getTargetGraph(), occupation);
        this.reverseDomain = Arrays.stream(data.getReverseCompatibility(filteringSettings, name)).map(TIntHashSet::new).collect(Collectors.toUnmodifiableList());
        this.domain = Arrays.stream(data.getCompatibility(filteringSettings, name)).map(TIntHashSet::new).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Pruner copy() {
        return new CachedZeroDomainPruner(this);
    }

    @Override
    public void afterReleaseVertex(int ignore, int v) {
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
        TIntSet candidates = reverseDomain.get(v);
        candidates.forEach(i -> {
            assert !domain.get(i).contains(v);
            domain.get(i).add(v);
            return true;
        });
    }

    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int v) {
        TIntSet candidates = reverseDomain.get(v);
        candidates.forEach(i -> {
            domain.get(i).remove(v);
            return true;
        });
    }

    private void afterOccupy(int verticesPlaced, int v) throws DomainCheckerException {
        TIntSet candidates = reverseDomain.get(v);
        candidates.forEach(i -> {
            domain.get(i).remove(v);
            return true;
        });
        boolean prune3 = domain.subList(verticesPlaced, domain.size()).stream().anyMatch(TIntSet::isEmpty);
        if (prune3) {
            candidates.forEach(i -> {
                domain.get(i).add(v);
                return true;
            });
            throw new DomainCheckerException("EmptyDomain constraint failed after occupying " + v);
        }
    }

    @Override
    public boolean isUnfruitfulCached(int verticesPlaced) {
        return domain.subList(verticesPlaced, domain.size()).stream().anyMatch(TIntSet::isEmpty);
    }


    @NotNull
    @Override
    public String toString() {
        return "CachedZeroDomainPruner{" + "domain=" + domain + '}';
    }
}
