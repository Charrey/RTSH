package com.charrey;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.settings.RunTimeCheck;
import com.charrey.settings.Settings;
import com.charrey.algorithms.UtilityData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.charrey.runtimecheck.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Occupation {

    @NotNull
    private final BitSet routingBits;
    @NotNull
    private final BitSet vertexBits;
    @NotNull
    public final DomainChecker domainChecker;

    public Occupation(@NotNull UtilityData data, int size, @NotNull Settings settings){
        this(data, size, settings.runTimeCheck, settings.initialLocalizedAllDifferent, settings.initialGlobalAllDifferent);
    }

    public Occupation(@NotNull UtilityData data, int size, int runTimeCheck, boolean initialLocalizedAllDifferent, boolean initialGlobalAllDifferent){
        switch (runTimeCheck) {
            case RunTimeCheck.NONE:
                domainChecker = new DummyDomainChecker();
                break;
            case RunTimeCheck.EMPTY_DOMAIN:
                domainChecker = new EmptyDomainChecker(data, initialLocalizedAllDifferent, initialGlobalAllDifferent);
                break;
            case RunTimeCheck.ALL_DIFFERENT:
                domainChecker = new AllDifferentChecker(data, initialLocalizedAllDifferent, initialGlobalAllDifferent);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        this.routingBits = new BitSet(size);
        this.vertexBits = new BitSet(size);
    }

    public void occupyRoutingAndCheck(int verticesPlaced, @NotNull Vertex v) throws DomainCheckerException {
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

    public void occupyRoutingAndCheck(int verticesPlaced, @NotNull Path p) throws DomainCheckerException {
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

    public void occupyVertex(int source, @NotNull Vertex target) throws DomainCheckerException {
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


    public void releaseRouting(int verticesPlaced, @NotNull Vertex v) {
        assert isOccupiedRouting(v);
        routingBits.clear(v.data());
        domainChecker.afterReleaseEdge(verticesPlaced, v);
    }

    public void releaseVertex(int verticesPlaced, @NotNull Vertex v) {
        assert vertexBits.get(v.data());
        vertexBits.clear(v.data());
        domainChecker.afterReleaseVertex(verticesPlaced, v);
    }

    public boolean isOccupiedRouting(@NotNull Vertex v) {
        return routingBits.get(v.data());
    }

    public boolean isOccupiedVertex(@NotNull Vertex v) {
        return vertexBits.get(v.data());
    }

    public boolean isOccupied(@NotNull Vertex v) {
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

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Occupation that = (Occupation) o;
        return routingBits.equals(that.routingBits) &&
                vertexBits.equals(that.vertexBits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routingBits, vertexBits);
    }

    public BitSet getRoutingOccupied() {
        return this.routingBits;
    }
}
