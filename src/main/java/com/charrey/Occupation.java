package com.charrey;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.checker.DomainChecker;
import com.charrey.util.datastructures.checker.DomainCheckerException;
import com.charrey.util.datastructures.checker.EmptyDomainChecker;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Occupation {

    private final List<Path> routingBits;
    private final BitSet vertexBits;
    public final DomainChecker domainCheckerLoose;

    public Occupation(UtilityData data, int size){
        domainCheckerLoose = new EmptyDomainChecker(data);
        this.routingBits = new ArrayList<>(size);
        this.vertexBits = new BitSet(size);
        for (int i = 0; i < size; i++) {
            routingBits.add(null);
        }
    }

    public void occupyRoutingAndCheck(int verticesPlaced, Vertex v, Path path) throws DomainCheckerException {
        assert routingBits.get(v.data()) == null;
        routingBits.set(v.data(), path);
        try {
            domainCheckerLoose.afterOccupyEdge(this, verticesPlaced, v);
        } catch (DomainCheckerException e) {
            routingBits.set(v.data(), null);
            throw e;
        }
    }

    public void occupyRoutingAndCheck(int verticesPlaced, Path path) throws DomainCheckerException {
        ListIterator<Vertex> it = path.intermediate().listIterator();
        List<Path> previous = new ArrayList<>(routingBits);
        while (it.hasNext()) {
            Vertex item = it.next();
            String previousCheck = null;
            try {
                previousCheck = this.domainCheckerLoose.toString();
                occupyRoutingAndCheck(verticesPlaced, item, path);
                assert isOccupiedRouting(item);
            } catch (DomainCheckerException e) {
                assertEquals(previousCheck, domainCheckerLoose.toString());
                it.previous();
                while (it.hasPrevious()) {
                    item = it.previous();
                    releaseRouting(verticesPlaced, item);
                }
                assertEquals(previous, routingBits);
                throw e;
            }
        }
    }

    public void occupyVertex(int source, Vertex target) throws DomainCheckerException {
        assert routingBits.get(target.data()) == null;
        assert !vertexBits.get(target.data());
        vertexBits.set(target.data());
        try {
            domainCheckerLoose.afterOccupyVertex(this, source, target);
        } catch (DomainCheckerException e) {
            vertexBits.clear(target.data());
            throw e;
        }
    }


    public void releaseRouting(int verticesPlaced, Vertex v) {
        assert isOccupiedRouting(v);
        routingBits.set(v.data(), null);
        domainCheckerLoose.afterReleaseEdge(this, verticesPlaced, v);
    }

    public void releaseVertex(int verticesPlaced, Vertex v) {
        assert vertexBits.get(v.data());
        vertexBits.clear(v.data());
        domainCheckerLoose.afterReleaseVertex(this, verticesPlaced, v);
    }

    public boolean isOccupiedRouting(Vertex v) {
        Path gotten = routingBits.get(v.data());
        return gotten != null;
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
        for (Path i : this.routingBits) {
            if (i != null) {
                myList.addAll(i.intermediate().stream().mapToInt(Vertex::data).boxed().collect(Collectors.toSet()));
            }
        }
        myList.addAll(this.vertexBits.stream().boxed().collect(Collectors.toSet()));
        assert myList.stream().allMatch(x -> isOccupied(new Vertex(x)));
        List<Integer> res = new LinkedList<>(myList);
        Collections.sort(res);
        return res.toString();

    }
}
