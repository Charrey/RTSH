package com.charrey;

import com.charrey.graph.Vertex;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.DomainChecker;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;

public class Occupation {

    private final BitSet routingBits;
    private final BitSet vertexBits;
    private final DomainChecker domainChecker;

    public Occupation(UtilityData data, int size){
        this.domainChecker = new DomainChecker(data);
        this.routingBits = new BitSet(size);
        this.vertexBits = new BitSet(size);
    }

    public Occupation(Occupation copy) {
        this.routingBits = (BitSet) copy.routingBits.clone();
        this.vertexBits = (BitSet) copy.vertexBits.clone();
        domainChecker = copy.domainChecker;
    }

    public void occupyRouting(int verticesPlaced, Vertex v) throws DomainChecker.EmptyDomainException {
        assert !routingBits.get(v.data());
        routingBits.set(v.data());
        try {
            domainChecker.afterOccupy(verticesPlaced, v);
        } catch (DomainChecker.EmptyDomainException e) {
            routingBits.clear(v.data());
            throw e;
        }
    }

    public void occupyRouting(int verticesPlaced, List<Vertex> vs) throws DomainChecker.EmptyDomainException {
        ListIterator<Vertex> iterator = vs.listIterator();
        while (iterator.hasNext()) {
            Vertex v = iterator.next();
            try {
                occupyRouting(verticesPlaced, v);
            } catch (DomainChecker.EmptyDomainException e) {
                iterator.previous();
                while (iterator.hasPrevious()) {
                    v = iterator.previous();
                    releaseRouting(verticesPlaced, v);
                }
                throw e;
            }
        }
    }

    public void occupyVertex(int source, Vertex target) throws DomainChecker.EmptyDomainException {
        assert !routingBits.get(target.data());
        assert !vertexBits.get(target.data());
        vertexBits.set(target.data());
        try {
            domainChecker.afterOccupy(source, target);
        } catch (DomainChecker.EmptyDomainException e) {
            vertexBits.clear(target.data());
            throw e;
        }
    }


    public void releaseRouting(int verticesPlaced, Vertex v) {
        assert routingBits.get(v.data());
        routingBits.clear(v.data());
        domainChecker.afterRelease(verticesPlaced, v);
    }

    public void releaseVertex(int verticesPlaced, Vertex v) {
        assert vertexBits.get(v.data());
        vertexBits.clear(v.data());
        domainChecker.afterRelease(verticesPlaced, v);
    }

    public boolean isOccupiedRouting(Vertex v) {
        return routingBits.get(v.data());
    }

    public boolean isOccupiedVertex(Vertex v) {
        return vertexBits.get(v.data());
    }

    public boolean isOccupied(Vertex v) {
        return isOccupiedRouting(v) || isOccupiedVertex(v);
    }


    private final List<OccupationListener> listeners = new ArrayList<>();
    public void register(OccupationListener listener) {
        listeners.add(listener);
    }
}
