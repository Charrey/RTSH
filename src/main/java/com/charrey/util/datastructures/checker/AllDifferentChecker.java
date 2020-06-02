package com.charrey.util.datastructures.checker;

import com.charrey.Occupation;
import com.charrey.algorithms.AllDifferent;
import com.charrey.graph.Vertex;
import com.charrey.util.UtilityData;

import java.lang.reflect.Array;
import java.util.*;

public class AllDifferentChecker extends DomainChecker {

    private final List<Vertex> order;
    AllDifferent allDifferent;
    private final Set<Vertex>[] domain;
    private final Vertex[][] reverseDomain;
    private Deque<Set<Vertex>>[] vertexState;

    private void pushVertex(int data) {
        vertexState[data].push(new HashSet<>(domain[data]));
    }

    private void popVertex(int data) {
        domain[data] = vertexState[data].pop();
    }


    @SuppressWarnings({"unchecked"})
    public AllDifferentChecker(UtilityData data) {
        this.order = data.getOrder();
        this.allDifferent = new AllDifferent();
        reverseDomain = data.getReverseCompatibility();
        this.domain = Arrays.stream(data.getCompatibility()).map(x -> new HashSet<>(Arrays.asList(x))).toArray(value -> (Set<Vertex>[])new Set[value]);
        vertexState = (Deque<Set<Vertex>>[]) Array.newInstance(Deque.class, domain.length);
        for (int i = 0; i < domain.length; i++) {
            vertexState[i] = new LinkedList<>();
        }
    }


    @Override
    public void afterReleaseVertex(Occupation occupation, int verticesPlaced, Vertex v) {
        Vertex[] candidates = reverseDomain[v.data()];
        for (int i = candidates.length - 1; i >= 0 && candidates[i].data() > verticesPlaced; i--) {
            assert !domain[candidates[i].data()].contains(v);
            domain[candidates[i].data()].add(v);
        }
        domain[verticesPlaced].addAll(vertexState[verticesPlaced].pop());
    }

    @Override
    public void afterReleaseEdge(Occupation occupation, int verticesPlaced, Vertex v) {
        Vertex[] candidates = reverseDomain[v.data()];
        for (int i = candidates.length - 1; i >= 0 && candidates[i].data() >= verticesPlaced; i--) {
            //assert !domain[candidates[i].data()].contains(v) : "The domain of " + candidates[i].data() + " should not contain " + v + " but it does.";
            domain[candidates[i].data()].add(v);
        }
    }

    @Override
    public void beforeOccupyVertex(Occupation occupation, int verticesPlaced, Vertex v) throws DomainCheckerException {
        Vertex[] candidates = reverseDomain[v.data()];
        int sourceVertexData = verticesPlaced - 1;
        pushVertex(sourceVertexData);
        domain[sourceVertexData].clear();
        domain[sourceVertexData].add(v);
        for (int i = candidates.length - 1; i >= 0 && candidates[i].data() > sourceVertexData; i--) {
            assert domain[candidates[i].data()].contains(v);
            domain[candidates[i].data()].remove(v);
        }
        if (!checkOK(verticesPlaced)) {
            for (int i = candidates.length - 1; i >= 0 && candidates[i].data() > sourceVertexData; i--) {
                domain[candidates[i].data()].add(v);
            }
            popVertex(sourceVertexData);
            throw new DomainCheckerException("AllDifferent constraint failed after occupying vertex " + v);
        }
    }



    @Override
    public void afterOccupyEdge(Occupation occupation, int verticesPlaced, Vertex v) throws DomainCheckerException {
        Vertex[] candidates = reverseDomain[v.data()];
        int sourceVertexData = verticesPlaced - 1;
        for (int i = candidates.length - 1; i >= 0 && candidates[i].data() > sourceVertexData; i--) {
            assert domain[candidates[i].data()].contains(v);
            domain[candidates[i].data()].remove(v);
        }
        if (!checkOK(verticesPlaced)) {
            for (int i = candidates.length - 1; i >= 0 && candidates[i].data() > sourceVertexData; i--) {
                domain[candidates[i].data()].add(v);
            }
            throw new DomainCheckerException("AllDifferent constraint failed after occupying routing vertex " + v);
        }
    }


    @Override
    public boolean checkOK(int verticesPlaced) {
        return Arrays.stream(domain).noneMatch(Set::isEmpty) &&  allDifferent.get(Arrays.asList(domain));
    }

    @Override
    public String toString() {
        return "AllDifferentChecker{domain=" + Arrays.toString(domain) +
                '}';
    }
}
