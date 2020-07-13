package com.charrey.matching;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * A class that saves which source graph vertex is mapped to which target graph vertex, and provides methods to facilitate
 * such matchings.
 */
public class VertexMatching {


    private final ArrayList<Integer> placement = new ArrayList<>();
    private final int[][] candidates;          //for each candidate vertex i, candidates[i] lists all its compatible target vertices.
    //@NotNull
    //private final int[] candidateToChooseNext;    //for each candidate vertex i, lists what target vertex to choose next.

    private final VertexCandidateIterator[] candidates2;

    private final GlobalOccupation occupation;
    private Consumer<Integer> onDeletion;


    /**
     * Instantiates a new Vertexmatching.
     *
     * @param data        the utility data class of this test case (for cached computations)
     * @param sourceGraph the source graph (doesnt matter whether old or new)
     * @param occupation  the global occupation which vertices have been used and which are available
     */
    public VertexMatching(@NotNull UtilityData data, @NotNull MyGraph sourceGraph, @NotNull MyGraph targetGraph, GlobalOccupation occupation, FilteringSettings filteringSettings, String name) {
        this.candidates = data.getCompatibility(filteringSettings, name);
        //todo: remove
        //this.candidateToChooseNext = new int[sourceGraph.vertexSet().size()];
        this.candidates2 = new VertexCandidateIterator[sourceGraph.vertexSet().size()];
        //todo: remove
        //assert candidateToChooseNext.length == candidates.length;
        //todo: remove
        //Arrays.fill(candidateToChooseNext, 0);
        IntStream.range(0, candidates2.length).forEach(i -> candidates2[i] = new VertexCandidateIterator(sourceGraph, targetGraph, i, filteringSettings, occupation));
        this.occupation = occupation;
    }

    /**
     * Returns whether a next vertex-on-vertex candidate pair exists. Note that it is not guaranteed that this pair
     * results in a valid matching or that it does not trigger pruning methods.
     *
     * @return whether a next vertex-on-vertex candidate pair exists.
     */
    public boolean canPlaceNext() {
        if (placement.size() >= candidates.length) {
            return false;
        } else {
            return candidates2[placement.size()].hasNext();
            //return !(candidateToChooseNext[placement.size()] >= candidates[placement.size()].length);
        }
    }

    /**
     * Tries the next partial vertex matching that does not trigger pruning methods. This initially attempts to place
     * a new vertex, but if no candidate vertex exists that is valid, this may also remove one or more already placed vertices
     * and try their next candidate pair.
     */
    public void placeNext() {
        assert canPlaceNext();
        while (!candidates2[placement.size()].hasNext()) {
            //while (candidateToChooseNext[placement.size()] >= candidates[placement.size()].length) {
            removeLast();
        }
        int toAdd = candidates2[placement.size()].next();
        //int toAdd = candidates[placement.size()][candidateToChooseNext[placement.size()]];
        boolean occupied = occupation.isOccupied(toAdd);
        if (occupied) {
            //todo: remove
            //candidateToChooseNext[placement.size()] += 1;
            if (canPlaceNext()) {
                placeNext();
            }
        } else {
            try {
                occupation.occupyVertex(placement.size() + 1, toAdd);
                placement.add(toAdd);
            } catch (DomainCheckerException e) {
                //todo: remove
                //candidateToChooseNext[placement.size()] += 1;
                if (canPlaceNext()) {
                    placeNext();
                }
            }
        }
    }


    /**
     * Returns the current vertex placement.
     *
     * @return the vertex placement
     */
    @NotNull
    public List<Integer> getPlacement() {
        return Collections.unmodifiableList(placement);
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
            //if (placement.size() < candidateToChooseNext.length) { //if every vertex was matched
            //this.candidateToChooseNext[placement.size()] = 0;
            this.candidates2[placement.size()].reset();
        }

        final int removed = placement.remove(placement.size() - 1);
        this.occupation.releaseVertex(placement.size(), removed);
        this.onDeletion.accept(removed);

        //todo: remove
        //this.candidateToChooseNext[placement.size()] += 1;
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
            //if (placement.size() < candidateToChooseNext.length) {
            this.candidates2[placement.size()].reset();
            //this.candidateToChooseNext[placement.size()] = 0;
        }
    }
}
