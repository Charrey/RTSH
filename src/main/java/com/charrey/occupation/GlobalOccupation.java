package com.charrey.occupation;

import com.charrey.algorithms.UtilityData;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.matching.VertexMatching;
import com.charrey.pruning.*;
import com.charrey.pruning.serial.PartialMatching;
import com.charrey.settings.Settings;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class that tracks for an entire homeomorphism source which target graph vertices are used up for which goal.
 * This class also throws exceptions when usage of a vertex would result in a dead end in the search.
 */
public class GlobalOccupation implements ReadOnlyOccupation {

    @NotNull
    private final Set<Integer> routingBits;
    @NotNull
    private final Set<Integer> vertexBits;
    private final Settings settings;

    private Pruner pruner;
    private final UtilityData data;

    /**
     * Instantiates a new Global occupation.
     *
     * @param data the data
     */
    public GlobalOccupation(UtilityData data, Settings settings) {
        this.data = data;
        this.settings = settings;
        this.routingBits = new HashSet<>();
        this.vertexBits = new HashSet<>();
    }

    public void init(VertexMatching vertexMatching) {
        this.pruner = PrunerFactory.get(settings, vertexMatching, data, this);
    }

    public void close() {
        if (pruner !=null) {
            this.pruner.close();
        }
    }

    /**
     * Provides a new transaction that may be used to occupy vertices used for routing using a commit system
     *
     * @return the transaction
     */
    public OccupationTransaction getTransaction() {
        Pruner prunerCopy = pruner.copy();
        OccupationTransaction toReturn;
        synchronized (routingBits) {
            toReturn = new OccupationTransaction(new TIntHashSet(routingBits), new TIntHashSet(vertexBits), prunerCopy, data, this);
        }
        prunerCopy.setOccupation(toReturn);
        return toReturn;
    }


    /**
     * Occupies a vertex in the target graph and marks it as being used as 'intermediate' vertex.
     *
     * @param vertexPlacementSize the number of source graph vertices placed
     * @param vertex              the vertex being occupied for routing purposes
     */
    void occupyRoutingWithoutCheck(int vertexPlacementSize, int vertex) {
        synchronized (routingBits) {
            assert !routingBits.contains(vertex);
            routingBits.add(vertex);
        }
        pruner.afterOccupyEdgeWithoutCheck(vertexPlacementSize, vertex);
    }

    public void occupyRoutingAndCheck(int vertexPlacementSize, int vertex, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        synchronized (routingBits) {
            assert !routingBits.contains(vertex);
            routingBits.add(vertex);
        }
        pruner.afterOccupyEdge(vertexPlacementSize, vertex, partialMatching);
    }


    /**
     * Occupies vertex for vertex-on-vertex matching.
     *
     * @param source the source graph vertex
     * @param target the target graph vertex
     * @throws DomainCheckerException thrown when this occupation would result in a dead end in the search. If this is thrown, this class remains unchanged.
     */
    public void occupyVertex(Integer source, Integer target, PartialMatching partialMatching) throws DomainCheckerException {
        synchronized (routingBits) {
            synchronized (vertexBits) {
                if (routingBits.contains(target) || vertexBits.contains(target)) {
                    throw new IllegalStateException();
                }
            }
        }
        List<Integer> hypothetical = new ArrayList<>(partialMatching.getVertexMapping());
        hypothetical.add(target);
        pruner.beforeOccupyVertex(source, target, () -> new PartialMatching(hypothetical, partialMatching.getEdgeMapping(), partialMatching.getPartialPath()));
        vertexBits.add(target);//todo lazier
    }


    /**
     * Unregister a vertex that was initially marked as used as intermediate vertex.
     *
     * @param vertexPlacementSize the number of source graph vertices placed
     * @param vertex              the vertex that is being unregistered
     * @throws IllegalArgumentException thrown when the vertex was not occupied for routing
     */
    void releaseRouting(int vertexPlacementSize, int vertex, PartialMatchingProvider partialMatchingProvider) {
        assert isOccupiedRouting(vertex);
        if (!isOccupiedRouting(vertex)) {
            throw new IllegalArgumentException("Cannot release a vertex that was never occupied (for routing purposes): " + vertex);
        }
        synchronized (routingBits) {
            routingBits.remove(vertex);
        }
        pruner.afterReleaseEdge(vertexPlacementSize, vertex, partialMatchingProvider);
    }

    /**
     * Unregister a vertex that was initially marked as used for vertex-on-vertex matching
     *
     * @param vertexPlacementSize the number of source graph vertices placed
     * @param vertex              the vertex that is being unregistered
     * @throws IllegalArgumentException thrown when the vertex was not occupied for vertex-on-vertex matching
     */
    public void releaseVertex(int vertexPlacementSize, int vertex, PartialMatchingProvider partialMatchingProvider) {
        if (!isOccupiedVertex(vertex)) {
            throw new IllegalArgumentException("Cannot release a vertex that was never occupied (for vertex-on-vertex purposes): " + vertex);
        }
        synchronized (vertexBits) {
            vertexBits.remove(vertex);
        }
        pruner.afterReleaseVertex(vertexPlacementSize, vertex, partialMatchingProvider);
    }

    /**
     * Returns whether some target graph vertex is currently occupied as intermediate vertex
     *
     * @param vertex the vertex to query
     * @return whether in the current matching this vertex is used as intermediate vertex
     */
    @Override
    public boolean isOccupiedRouting(Integer vertex) {
        synchronized (routingBits) {
            return routingBits.contains(vertex);
        }
    }

    /**
     * Returns whether some target graph vertex is currently occupied as vertex-on-vertex matching vertex
     *
     * @param vertex the vertex to query
     * @return whether in the current matching this vertex is used for vertex-on-vertex matching
     */
    public boolean isOccupiedVertex(Integer vertex) {
        synchronized (vertexBits) {
            return vertexBits.contains(vertex);
        }
    }

    public boolean isOccupied(int vertex) {
        try {
            return isOccupiedRouting(vertex) || isOccupiedVertex(vertex);
        } catch (ArrayIndexOutOfBoundsException e) { //parallel
            return false;
        }
    }

    @Override
    public String toString() {
        TIntSet myList = new TIntHashSet();
        myList.addAll(IntStream.range(0, routingBits.size()).filter(this.routingBits::contains).boxed().collect(Collectors.toSet()));
        myList.addAll(this.vertexBits);
        TIntList res = new TIntArrayList(myList);
        res.sort();
        return res.toString();

    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalOccupation that = (GlobalOccupation) o;
        synchronized (routingBits) {
            synchronized (vertexBits) {
                return routingBits.equals(that.routingBits) &&
                        vertexBits.equals(that.vertexBits);
            }
        }
    }

    @Override
    public int hashCode() {
        synchronized (routingBits) {
            synchronized (vertexBits) {
                return Objects.hash(routingBits, vertexBits);
            }
        }
    }

    /**
     * Returns the set of target graph vertices that have been marked as being used as intermediate vertex.
     *
     * @return the set of vertices being used as intermediate vertex.
     */
    public TIntSet getRoutingOccupied() {
        synchronized (routingBits) {
            return new TIntHashSet(this.routingBits);
        }
    }


}
