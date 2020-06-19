package com.charrey.runtimecheck;

import com.charrey.algorithms.AllDifferent;
import com.charrey.algorithms.UtilityData;
import com.charrey.graph.Vertex;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class AllDifferentChecker extends DomainChecker {

    @NotNull
    private final AllDifferent allDifferent;
    @NotNull
    private final Set<Vertex>[] domain;
    private final Vertex[][] reverseDomain;
    @NotNull
    private final Deque<Set<Vertex>>[] vertexState;

    @SuppressWarnings("unchecked")
    private AllDifferentChecker(AllDifferentChecker copyOf) {
        super();
        reverseDomain = new Vertex[copyOf.reverseDomain.length][];
        for (int i = 0; i < copyOf.reverseDomain.length; i++) {
            reverseDomain[i] = copyOf.reverseDomain[i].clone();
        }
        this.allDifferent = copyOf.allDifferent;
        domain = (Set<Vertex>[]) Array.newInstance(Set.class, copyOf.domain.length);
        for (int i = 0; i < copyOf.domain.length; i++) {
            domain[i] = new HashSet<>(copyOf.domain[i]);
        }
        vertexState = (Deque<Set<Vertex>>[]) Array.newInstance(Deque.class, copyOf.vertexState.length);
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


    @SuppressWarnings({"unchecked"})
    public AllDifferentChecker(@NotNull UtilityData data, boolean initialNeighbourhoodFiltering, boolean initialGlobalAllDifferent) {
        this.allDifferent = new AllDifferent();
        reverseDomain = data.getReverseCompatibility(initialNeighbourhoodFiltering, initialGlobalAllDifferent);
        this.domain = Arrays.stream(data.getCompatibility(initialNeighbourhoodFiltering, initialGlobalAllDifferent)).map(x -> new HashSet<>(Arrays.asList(x))).toArray(value -> (Set<Vertex>[]) new Set[value]);
        vertexState = (Deque<Set<Vertex>>[]) Array.newInstance(Deque.class, domain.length);
        for (int i = 0; i < domain.length; i++) {
            vertexState[i] = new LinkedList<>();
        }
    }


    @Override
    public void afterReleaseVertex(int verticesPlaced, @NotNull Vertex v) {
        Vertex[] candidates = reverseDomain[v.data()];
        for (int i = candidates.length - 1; i >= 0 && candidates[i].data() > verticesPlaced; i--) {
            assert !domain[candidates[i].data()].contains(v);
            domain[candidates[i].data()].add(v);
        }
        domain[verticesPlaced].addAll(vertexState[verticesPlaced].pop());
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, @NotNull Vertex v) {
        Vertex[] candidates = reverseDomain[v.data()];
        for (int i = candidates.length - 1; i >= 0 && candidates[i].data() >= verticesPlaced; i--) {
            domain[candidates[i].data()].add(v);
        }
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, @NotNull Vertex v) throws DomainCheckerException {
        Vertex[] candidates = reverseDomain[v.data()];
        int sourceVertexData = verticesPlaced - 1;
        pushVertex(sourceVertexData);
        domain[sourceVertexData].clear();
        domain[sourceVertexData].add(v);
        removeFromDomains(v, candidates, sourceVertexData);
        if (!checkOK(verticesPlaced)) {
            for (int i = candidates.length - 1; i >= 0 && candidates[i].data() > sourceVertexData; i--) {
                domain[candidates[i].data()].add(v);
            }
            popVertex(sourceVertexData);
            throw new DomainCheckerException("AllDifferent constraint failed after occupying vertex " + v);
        }
    }



    @Override
    public void afterOccupyEdge(int verticesPlaced, @NotNull Vertex v) throws DomainCheckerException {
        Vertex[] candidates = reverseDomain[v.data()];
        int sourceVertexData = verticesPlaced - 1;
        removeFromDomains(v, candidates, sourceVertexData);
        if (!checkOK(verticesPlaced)) {
            for (int i = candidates.length - 1; i >= 0 && candidates[i].data() > sourceVertexData; i--) {
                domain[candidates[i].data()].add(v);
            }
            throw new DomainCheckerException("AllDifferent constraint failed after occupying routing vertex " + v);
        }
    }

    private void removeFromDomains(Vertex placedTarget, Vertex[] sourcegraphCandidates, int sourceVertexData) {
        for (int i = sourcegraphCandidates.length - 1; i >= 0 && sourcegraphCandidates[i].data() > sourceVertexData; i--) {
            assert domain[sourcegraphCandidates[i].data()].contains(placedTarget);
            domain[sourcegraphCandidates[i].data()].remove(placedTarget);
        }
    }


    @Override
    public boolean checkOK(int verticesPlaced) {
        return Arrays.stream(domain).noneMatch(Set::isEmpty) &&  allDifferent.get(Arrays.asList(domain));
    }



    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public String toString() {
        List[] domainString = new List[domain.length];
        for (int i = 0; i < domainString.length; i++) {
            domainString[i] = new ArrayList(domain[i]);
            domainString[i].sort(Comparator.comparingInt(o -> ((Vertex) o).data()));
        }
        return "AllDifferentChecker{domain=" + Arrays.toString(domainString) +
                '}';
    }
}
