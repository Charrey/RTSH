package com.charrey;

import com.charrey.exceptions.EmptyDomainException;
import com.charrey.graph.Vertex;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.DomainChecker;

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

    public void occupyRouting(int verticesPlaced, Vertex v) {
        assert !routingBits.get(v.data());
        routingBits.set(v.data());
        try {
            domainChecker.afterOccupy(verticesPlaced, v);
        } catch (EmptyDomainException ignored) {
        }
    }

    public void occupyRoutingAndCheck(int verticesPlaced, Vertex v) throws EmptyDomainException {
        assert !routingBits.get(v.data());
        routingBits.set(v.data());
        domainChecker.afterOccupy(verticesPlaced, v);
    }

    public void occupyRouting(int verticesPlaced, List<Vertex> vs)  {
        for (Vertex v : vs) {
            occupyRouting(verticesPlaced, v);
        }
    }

    public void occupyRoutingAndCheck(int verticesPlaced, List<Vertex> vs) throws EmptyDomainException {
        ListIterator<Vertex> it = vs.listIterator();
        while (it.hasNext()) {
            Vertex item = it.next();
            try {
                occupyRoutingAndCheck(verticesPlaced, item);
            } catch (EmptyDomainException e) {
                while (it.hasPrevious()) {
                    item = it.previous();
                    releaseRouting(verticesPlaced, item);
                }
                throw new EmptyDomainException();
            }
        }
    }
    //2m8s

//    public void occupyRoutingAndCheck(int verticesPlaced, List<Vertex> vs) throws EmptyDomainException {
//        ListIterator<Vertex> it = vs.listIterator();
//        while (it.hasNext()) {
//            Vertex item = it.next();
//            try {
//                occupyRoutingAndCheck(verticesPlaced, item);
//            } catch (EmptyDomainException e) {
//                while (it.hasPrevious()) {
//                    item = it.previous();
//                    releaseRouting(verticesPlaced, item);
//                }
//                throw new EmptyDomainException();
//            }
//        }
//    }

    public void occupyVertex(int source, Vertex target) throws EmptyDomainException {
        assert !routingBits.get(target.data());
        assert !vertexBits.get(target.data());
        vertexBits.set(target.data());
        try {
            domainChecker.afterOccupy(source, target);
        } catch (EmptyDomainException e) {
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


    public BitSet getRoutingBits() {
        return routingBits;
    }
}
