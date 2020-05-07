package com.charrey.util.datastructures.checker;

import com.charrey.Occupation;
import com.charrey.algorithms.AllDifferent;
import com.charrey.graph.Vertex;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.IndexMap;
import com.charrey.util.datastructures.LinkedIndexSet;

import java.lang.reflect.Array;
import java.util.*;

public class AllDifferentChecker extends DomainChecker {

    private final List<Vertex> order;
    AllDifferent allDifferent;
    private final Map<Vertex, Set<Vertex>> domain;
    private final Vertex[][] reverseDomain;
    private Deque<Set<Vertex>>[] vertexState;



    @SuppressWarnings("unchecked")
    public AllDifferentChecker(int graphSize, UtilityData data) {
        super();
        domain = new IndexMap<>(data.getOrder().size());
        vertexState = (Deque<Set<Vertex>>[]) Array.newInstance(Deque.class, graphSize);
        for (int i = 0; i < data.getCompatibility().length; i++) {
            Set<Vertex> setToAdd = new LinkedIndexSet<>(graphSize, Arrays.asList(data.getCompatibility()[i]), Vertex.class);
            domain.put(data.getOrder().get(i), setToAdd);
        }
        for (int i = 0; i < graphSize; i++) {
            vertexState[i] = new LinkedList<>();
        }
        this.reverseDomain = data.getReverseCompatibility();
        allDifferent = new AllDifferent();
        this.order = data.getOrder();
    }


    @Override
    public void afterReleaseVertex(Occupation occupation, int verticesPlaced, Vertex released) {
        domain.put(order.get(verticesPlaced), vertexState[verticesPlaced].pop());
        for (Vertex candidate : reverseDomain[released.data()]) {
            if (candidate.data() > verticesPlaced) {
                boolean added = domain.get(candidate).add(released);
                assert added;
            }
        }
    }

    @Override
    public void afterReleaseEdge(Occupation occupation, int verticesPlaced, Vertex released) {
        for (Vertex candidate : reverseDomain[released.data()]) {
            if (candidate.data() > verticesPlaced) {
                domain.get(candidate).add(released);
            }
        }
    }

    @Override
    public void afterOccupyVertex(Occupation occupation, int verticesPlaced, Vertex occupied) throws DomainCheckerException {
        vertexState[verticesPlaced - 1].push(new LinkedIndexSet<>(reverseDomain.length, domain.get(order.get(verticesPlaced - 1)), Vertex.class));
        domain.put(order.get(verticesPlaced - 1), new LinkedIndexSet<>(reverseDomain.length, Set.of(occupied), Vertex.class));
        Vertex[] candidates = reverseDomain[occupied.data()];
        for (Vertex candidate : candidates) {
            if (candidate.data() >= verticesPlaced) {
                domain.get(candidate).remove(occupied);
            }
        }
        if (!allDifferent.get(reverseDomain.length, new HashMap<>(domain))) {
            domain.put(order.get(verticesPlaced), vertexState[verticesPlaced - 1].pop());
            for (Vertex candidate : candidates) {
                if (candidate.data() >= verticesPlaced) {
                    domain.get(candidate).add(occupied);
                }
            }
            throw new DomainCheckerException();
        }
    }

    @Override
    public void afterOccupyEdge(Occupation occupation, int verticesPlaced, Vertex occupied) throws DomainCheckerException {
        Vertex[] candidates = reverseDomain[occupied.data()];
        for (Vertex candidate : candidates) {
            if (candidate.data() > verticesPlaced) {
                domain.get(candidate).remove(occupied);
            }
        }
        if (!allDifferent.get(reverseDomain.length, domainCopy())) {
            for (Vertex candidate : candidates) {
                if (candidate.data() > verticesPlaced) {
                    domain.get(candidate).add(occupied);
                }
            }
            throw new DomainCheckerException();
        }
    }

    @Override
    public boolean checkOK(int verticesPlaced) {
        throw new UnsupportedOperationException();
    }

    public boolean check() {
        return allDifferent.get(reverseDomain.length, domainCopy());
    }

    private Map<Vertex, Set<Vertex>> domainCopy() {
        Map<Vertex, Set<Vertex>> res = new HashMap<>();
        domain.forEach((key, value) -> res.put(key, new HashSet<>(value)));
        return res;
    }
}
