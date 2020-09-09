package com.charrey.matching;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.candidate.VertexCandidateIterator;
import com.charrey.matching.candidate.VertexCandidateIteratorFactory;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.serial.PartialMatching;
import com.charrey.settings.Settings;
import gnu.trove.TCollections;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * A class that saves which source graph vertex is mapped to which target graph vertex, and provides methods to facilitate
 * such matchings.
 */
public class VertexMatching implements Supplier<TIntList>, PartialMatchingProvider {

    private final TIntList placement = new TIntArrayList();

    private final VertexCandidateIterator[] candidates2;

    private final GlobalOccupation occupation;
    private Consumer<Integer> onDeletion;
    private Supplier<TIntObjectMap<Set<Path>>> edgeMatchingProvider;

    public void setEdgeMatchingProvider(Supplier<TIntObjectMap<Set<Path>>> provider) {
        this.edgeMatchingProvider = provider;
    }


    /**
     * Instantiates a new Vertexmatching.
     *
     * @param sourceGraph the source graph (doesnt matter whether old or new)
     * @param occupation  the global occupation which vertices have been used and which are available
     */
    public VertexMatching(@NotNull MyGraph sourceGraph, @NotNull MyGraph targetGraph, GlobalOccupation occupation, Settings settings) {
        //for each candidate vertex i, candidates[i] lists all its compatible target vertices.
        this.candidates2 = new VertexCandidateIterator[sourceGraph.vertexSet().size()];
        IntStream.range(0, candidates2.length).forEach(i -> candidates2[i] = VertexCandidateIteratorFactory.get(sourceGraph, targetGraph, settings, occupation, i, this));
        this.occupation = occupation;
    }

    /**
     * Returns whether a next vertex-on-vertex candidate pair exists. Note that it is not guaranteed that this pair
     * results in a valid matching or that it does not trigger pruning methods.
     *
     * @return whether a next vertex-on-vertex candidate pair exists.
     */
    public boolean canPlaceNext() {
        if (placement.size() >= candidates2.length) {
            return false;
        } else {
            return candidates2[placement.size()].hasNext();
        }
    }

    /**
     * Tries the next partial vertex matching that does not trigger pruning methods. This initially attempts to place
     * a new vertex, but if no candidate vertex exists that is valid, this may also remove one or more already placed vertices
     * and try their next candidate pair.
     * @return
     */
    public boolean placeNext() {
        assert canPlaceNext();
        while (!candidates2[placement.size()].hasNext()) {
            removeLast();
        }
        int toAdd = candidates2[placement.size()].next();
        boolean occupied = occupation.isOccupied(toAdd);
        if (occupied) {
            if (canPlaceNext()) {
                return placeNext();
            }
        } else {
            try {
                occupation.occupyVertex(placement.size() + 1, toAdd, getPartialMatching());
                placement.add(toAdd);
                return true;
            } catch (DomainCheckerException e) {
                if (canPlaceNext()) {
                    return placeNext();
                }
            }
        }
        return false;
    }


    /**
     * Returns the current vertex placement.
     *
     * @return the vertex placement
     */
    @NotNull
    public TIntList getPlacement() {
        return TCollections.unmodifiableList(placement);
    }


    @NotNull
    @Override
    public String toString() {
        return "State {\n" + "\tplacement:\t" + placement + "\n" +
                "}";
    }

    /**
     * Returns whether retrying the vertex placement is possible.
     *
     * @return whether retrying is possible.
     */
    public boolean canRetry() {
        return placement.size() > 0;
    }

    /**
     * Removes the last vertex-on-vertex match.
     */
    public void removeLast() {
        if (placement.size() < candidates2.length) { //if every vertex was matched
            this.candidates2[placement.size()].reset();
        }
        final int toRemove = placement.get(placement.size() - 1);
        placement.removeAt(placement.size() - 1);
        this.occupation.releaseVertex(placement.size(), toRemove);
        this.onDeletion.accept(toRemove);
    }

    /**
     * Sets a function to be executed whenever a vertex-on-vertex match is removed.
     *
     * @param function the function that is executed
     */
    void setOnDeletion(Consumer<Integer> function) {
        this.onDeletion = function;
    }

    /**
     * Sets that for the next source graph vertex, every target graph vertex in its domain may be tried (even if that has
     * happened before).
     */
    public void giveAllowance() {
        if (placement.size() < candidates2.length) {
            this.candidates2[placement.size()].doReset();
        }
    }

    @Override
    public PartialMatching getPartialMatching() {
        return new PartialMatching(placement, edgeMatchingProvider.get(), new TIntHashSet());
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public TIntList get() {
        return new TIntArrayList(placement);
    }
}
