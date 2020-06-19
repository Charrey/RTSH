package com.charrey.runtimecheck;

import com.charrey.graph.Vertex;
import com.charrey.algorithms.UtilityData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EmptyDomainChecker extends DomainChecker {

    private final Vertex[][] reverseDomain;
    @NotNull
    private final Set<Vertex>[] domain;

    @Override
    public DomainChecker copy() {
        return new EmptyDomainChecker(this);
    }

    @SuppressWarnings("unchecked")
    private EmptyDomainChecker(EmptyDomainChecker copyFrom) {
        reverseDomain = new Vertex[copyFrom.reverseDomain.length][];
        for (int i = 0; i < copyFrom.reverseDomain.length; i++) {
            reverseDomain[i] = copyFrom.reverseDomain[i].clone();
        }
        domain = (Set<Vertex>[]) Array.newInstance(Set.class, copyFrom.domain.length);
        for (int i = 0; i < copyFrom.domain.length; i++) {
            domain[i] = new HashSet<>(copyFrom.domain[i]);
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public EmptyDomainChecker(@NotNull UtilityData data, boolean intialNeighbourHoodFiltering, boolean initialGlobalAllDifferent) {
        this.reverseDomain = data.getReverseCompatibility(intialNeighbourHoodFiltering, initialGlobalAllDifferent);
        this.domain = (Set[]) Arrays.stream(data.getCompatibility(intialNeighbourHoodFiltering, initialGlobalAllDifferent)).map(x -> new HashSet(Arrays.asList(x))).toArray(Set[]::new);
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, @NotNull Vertex v) {
        afterRelease(v);
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, @NotNull Vertex v) {
        afterRelease(v);
    }


    @Override
    public void beforeOccupyVertex(int verticesPlaced, @NotNull Vertex v) throws DomainCheckerException {
        afterOccupy(verticesPlaced, v);
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, @NotNull Vertex v) throws DomainCheckerException {
        afterOccupy(verticesPlaced, v);
    }

    private void afterRelease(@NotNull Vertex v) {
        Vertex[] candidates = reverseDomain[v.data()];
        for (int i = candidates.length - 1; i >= 0; i--) {
            assert !domain[candidates[i].data()].contains(v);
            domain[candidates[i].data()].add(v);
        }
    }

    private void afterOccupy(int verticesPlaced, @NotNull Vertex v) throws DomainCheckerException {
        Vertex[] candidates = reverseDomain[v.data()];
        for (int i = candidates.length - 1; i >= 0; i--) {
            domain[candidates[i].data()].remove(v);
        }
        if (Arrays.asList(domain).subList(verticesPlaced, domain.length).stream().anyMatch(Set::isEmpty)) {
            for (int i = candidates.length - 1; i >= 0; i--) {
                domain[candidates[i].data()].add(v);
            }
            throw new DomainCheckerException("EmptyDomain constraint failed after occupying " + v);
        }
    }

    @Override
    public boolean checkOK(int verticesPlaced) {
        return Arrays.asList(domain).subList(verticesPlaced, domain.length).stream().noneMatch(Set::isEmpty);
    }



    @NotNull
    @Override
    public String toString() {
        return "EmptyDomainChecker{" + "domain=" + Arrays.toString(domain) + '}';
    }
}
