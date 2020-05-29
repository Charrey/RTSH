package com.charrey.util.datastructures.checker;

import com.charrey.Occupation;
import com.charrey.graph.Vertex;
import com.charrey.util.UtilityData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EmptyDomainChecker extends DomainChecker {

    private final Vertex[][] reverseDomain;
    private final Set<Vertex>[] domain;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public EmptyDomainChecker(UtilityData data) {
        this.reverseDomain = data.getReverseCompatibility();
        this.domain = (Set[]) Arrays.stream(data.getCompatibility()).map(x -> new HashSet(Arrays.asList(x))).toArray(Set[]::new);
    }

    @Override
    public void afterReleaseVertex(Occupation occupation, int verticesPlaced, Vertex v) {
        Vertex[] candidates = reverseDomain[v.data()];
        for (int i = candidates.length - 1; i >= 0; i--) {
            assert !domain[candidates[i].data()].contains(v);
            domain[candidates[i].data()].add(v);
        }
    }

    @Override
    public void afterReleaseEdge(Occupation occupation, int verticesPlaced, Vertex v) {
        Vertex[] candidates = reverseDomain[v.data()];
        for (int i = candidates.length - 1; i >= 0; i--) {
            assert !domain[candidates[i].data()].contains(v);
            domain[candidates[i].data()].add(v);
        }
    }



    @Override
    public void beforeOccupyVertex(Occupation occupation, int verticesPlaced, Vertex v) throws DomainCheckerException {
        afterOccupy(verticesPlaced, v);
    }

    @Override
    public void afterOccupyEdge(Occupation occupation, int verticesPlaced, Vertex v) throws DomainCheckerException {
        afterOccupy(verticesPlaced, v);
    }

    private void afterOccupy(int verticesPlaced, Vertex v) throws DomainCheckerException {
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

    @Override
    public String toString() {
        return "EmptyDomainChecker{" + "domain=" + Arrays.toString(domain) + '}';
    }
}
