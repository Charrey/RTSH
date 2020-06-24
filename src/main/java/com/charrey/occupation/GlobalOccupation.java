package com.charrey.occupation;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.Path;
import com.charrey.runtimecheck.*;
import com.charrey.settings.RunTimeCheck;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalOccupation extends AbstractOccupation {

    @NotNull
    private final BitSet routingBits;
    @NotNull
    private final BitSet vertexBits;
    @NotNull
    public final DomainChecker domainChecker;

    public GlobalOccupation(@NotNull UtilityData data, int size, @NotNull Settings settings){
        this(data, size, settings.runTimeCheck, settings.initialNeighbourhoodFiltering, settings.initialGlobalAllDifferent);
    }

    public GlobalOccupation(@NotNull UtilityData data, int size, int runTimeCheck, boolean initialNeighbourHoodFiltering, boolean initialGlobalAllDifferent){
        switch (runTimeCheck) {
            case RunTimeCheck.NONE:
                domainChecker = new DummyDomainChecker();
                break;
            case RunTimeCheck.EMPTY_DOMAIN:
                domainChecker = new EmptyDomainChecker(data, initialNeighbourHoodFiltering, initialGlobalAllDifferent);
                break;
            case RunTimeCheck.ALL_DIFFERENT:
                domainChecker = new AllDifferentChecker(data, initialNeighbourHoodFiltering, initialGlobalAllDifferent);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        this.routingBits = new BitSet(size);
        this.vertexBits = new BitSet(size);
    }

    public OccupationTransaction getTransaction() {
       return new OccupationTransaction((BitSet) routingBits.clone(), (BitSet) vertexBits.clone(), domainChecker.copy(), this);
    }

    public void commitTransaction(OccupationTransaction transaction) {
        throw new UnsupportedOperationException();
    }

    public void uncommitTransaction(OccupationTransaction transaction) {
        throw new UnsupportedOperationException();
    }

    void occupyRoutingAndCheck(int verticesPlaced, int v) throws DomainCheckerException {
        assert !routingBits.get(v);
        routingBits.set(v);
        String previous = null;
        try {
            previous = domainChecker.toString();
            domainChecker.afterOccupyEdge(verticesPlaced, v);
        } catch (DomainCheckerException e) {
            assertEquals(previous, domainChecker.toString());
            routingBits.clear(v);
            throw e;
        }
    }

    void occupyRoutingAndCheck(int verticesPlaced, @NotNull Path p) throws DomainCheckerException {
        for (int i = 0; i < p.intermediate().size(); i++) {
            try {
                occupyRoutingAndCheck(verticesPlaced, p.intermediate().get(i));
            } catch (DomainCheckerException e) {
                for (int j = i - 1; j >= 0; j--) {
                    releaseRouting(verticesPlaced, p.intermediate().get(j));
                }
                throw e;
            }
        }
    }

    public void occupyVertex(int source, int target) throws DomainCheckerException {
        assert !routingBits.get(target);
        assert !vertexBits.get(target);
        vertexBits.set(target);
        try {
            domainChecker.beforeOccupyVertex(source, target);
        } catch (DomainCheckerException e) {
            vertexBits.clear(target);
            throw e;
        }
    }


    void releaseRouting(int verticesPlaced, int v) {
        assert isOccupiedRouting(v);
        routingBits.clear(v);
        domainChecker.afterReleaseEdge(verticesPlaced, v);
    }

    public void releaseVertex(int verticesPlaced, int v) {
        assert vertexBits.get(v);
        vertexBits.clear(v);
        domainChecker.afterReleaseVertex(verticesPlaced, v);
    }

    public boolean isOccupiedRouting(int v) {
        return routingBits.get(v);
    }

    public boolean isOccupiedVertex(int v) {
        return vertexBits.get(v);
    }

    public boolean isOccupied(int v) {
        return isOccupiedRouting(v) || isOccupiedVertex(v);
    }

    @Override
    public String toString() {
        Set<Integer> myList = new HashSet<>();
        myList.addAll(IntStream.range(0, routingBits.size()).filter(this.routingBits::get).boxed().collect(Collectors.toSet()));
        myList.addAll(this.vertexBits.stream().boxed().collect(Collectors.toSet()));
        assert myList.stream().allMatch(this::isOccupied);
        List<Integer> res = new LinkedList<>(myList);
        Collections.sort(res);
        return res.toString();

    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalOccupation that = (GlobalOccupation) o;
        return routingBits.equals(that.routingBits) &&
                vertexBits.equals(that.vertexBits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routingBits, vertexBits);
    }

    public BitSet getRoutingOccupied() {
        return (BitSet) this.routingBits.clone();
    }
}
