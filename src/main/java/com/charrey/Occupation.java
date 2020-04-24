package com.charrey;

import com.charrey.graph.Vertex;

import java.util.BitSet;

public class Occupation {

    private final BitSet routingBits;
    private final BitSet vertexBits;

    public Occupation(int size){
        this.routingBits = new BitSet(size);
        this.vertexBits = new BitSet(size);
    }

    public Occupation(Occupation copy) {
        this.routingBits = (BitSet) copy.routingBits.clone();
        this.vertexBits = (BitSet) copy.vertexBits.clone();
    }

    public void occupyRouting(Vertex v) {
        assert !routingBits.get(v.data());
        routingBits.set(v.data());
    }

    public void occupyVertex(Vertex v) {
        assert !routingBits.get(v.data());
        assert !vertexBits.get(v.data());
        vertexBits.set(v.data());
    }

    public void releaseRouting(Vertex v) {
        assert routingBits.get(v.data());
        routingBits.clear(v.data());
    }

    public void releaseVertex(Vertex v) {
        assert vertexBits.get(v.data());
        vertexBits.clear(v.data());
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


}
