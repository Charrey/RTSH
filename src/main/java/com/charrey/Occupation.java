package com.charrey;

import com.charrey.graph.Vertex;
import com.charrey.settings.RunTimeCheck;
import com.charrey.settings.Settings;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.checker.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Occupation {

    private final BitSet routingBits;
    private final BitSet vertexBits;
    public final DomainChecker domainChecker;

    public Occupation(UtilityData data, int size){
        switch (Settings.instance.runTimeCheck) {
            case RunTimeCheck.NONE:
                domainChecker = new DummyDomainChecker();
                break;
            case RunTimeCheck.EMPTY_DOMAIN:
                domainChecker = new EmptyDomainChecker(data);
                break;
            case RunTimeCheck.ALL_DIFFERENT:
                domainChecker = new AllDifferentChecker(data);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        this.routingBits = new BitSet(size);
        this.vertexBits = new BitSet(size);
    }

    public void occupyRoutingAndCheck(int verticesPlaced, Vertex v) throws DomainCheckerException {
        assert !routingBits.get(v.data());
        routingBits.set(v.data());
        String previous = null;
        try {
            previous = domainChecker.toString();
            domainChecker.afterOccupyEdge(verticesPlaced, v);
        } catch (DomainCheckerException e) {
            assertEquals(previous, domainChecker.toString());
            routingBits.clear(v.data());
            throw e;
        }
    }


    public void occupyVertex(int source, Vertex target) throws DomainCheckerException {
        assert !routingBits.get(target.data());
        assert !vertexBits.get(target.data());
        vertexBits.set(target.data());
        try {
            domainChecker.beforeOccupyVertex(source, target);
        } catch (DomainCheckerException e) {
            vertexBits.clear(target.data());
            throw e;
        }
    }


    public void releaseRouting(int verticesPlaced, Vertex v) {
        assert isOccupiedRouting(v);
        routingBits.clear(v.data());
        domainChecker.afterReleaseEdge(verticesPlaced, v);
    }

    public void releaseVertex(int verticesPlaced, Vertex v) {
        assert vertexBits.get(v.data());
        vertexBits.clear(v.data());
        domainChecker.afterReleaseVertex(verticesPlaced, v);
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

    @Override
    public String toString() {
        Set<Integer> myList = new HashSet<>();
        myList.addAll(IntStream.range(0, routingBits.size()).filter(this.routingBits::get).boxed().collect(Collectors.toSet()));
        myList.addAll(this.vertexBits.stream().boxed().collect(Collectors.toSet()));
        assert myList.stream().allMatch(x -> isOccupied(new Vertex(x)));
        List<Integer> res = new LinkedList<>(myList);
        Collections.sort(res);
        return res.toString();

    }
}
