package com.charrey.matching;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.runtimecheck.DomainCheckerException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VertexMatching {


    private final ArrayList<Integer> placement = new ArrayList<>();
    private final Integer[][] candidates;          //for each candidate vertex i, candidates[i] lists all its compatible target vertices.
    @NotNull
    private final int[] candidateToChooseNext;    //for each candidate vertex i, lists what target vertex to choose next.
    private final GlobalOccupation occupation;
    private DeletionFunction onDeletion;


    public VertexMatching(@NotNull UtilityData data, @NotNull MyGraph pattern, GlobalOccupation occupation, boolean initialLocalAllDifferent, boolean initialGlobalAllDifferent) {
        this.candidates = data.getCompatibility(initialLocalAllDifferent, initialGlobalAllDifferent);
        candidateToChooseNext = new int[pattern.vertexSet().size()];
        assert candidateToChooseNext.length == candidates.length;
        Arrays.fill(candidateToChooseNext, 0);
        this.occupation = occupation;
    }

    public boolean canPlaceNext() {
        if (placement.size() >= candidates.length) {
            return false;
        } else {
            return !(candidateToChooseNext[placement.size()] >= candidates[placement.size()].length);
        }
    }

    public void placeNext() {
        assert canPlaceNext();
        while (candidateToChooseNext[placement.size()] >= candidates[placement.size()].length) {
            removeLast();
        }
        int toAdd = candidates[placement.size()][candidateToChooseNext[placement.size()]];
        boolean occupied = occupation.isOccupied(toAdd);
        if (occupied) {
            candidateToChooseNext[placement.size()] += 1;
            if (canPlaceNext()) {
                placeNext();
            }
        } else {
            try {
                occupation.occupyVertex(placement.size() + 1, toAdd);
                placement.add(toAdd);
            } catch (DomainCheckerException e) {
                candidateToChooseNext[placement.size()] += 1;
                if (canPlaceNext()) {
                    placeNext();
                    return;
                } else {
                    return;
                }
            }
        }
    }


    @NotNull
    public List<Integer> getPlacementUnsafe() {
        return Collections.unmodifiableList(placement);
    }


    @NotNull
    @Override
    public String toString() {
        return "State {\n" + "\tplacement:\t" + placement + "\n" +
                "}";
    }

    public boolean canRetry() {
        return placement.size() > 0;
    }

    public void removeLast() {
        if (placement.size() < candidateToChooseNext.length) {
            candidateToChooseNext[placement.size()] = 0;
        }
        int removed = placement.remove(placement.size()-1);
        occupation.releaseVertex(placement.size(), removed);
        this.onDeletion.run(removed);
        candidateToChooseNext[placement.size()] += 1;
    }

    void setOnDeletion(DeletionFunction x) {
        this.onDeletion = x;
    }

    public void giveAllowance() {
        if (placement.size() < candidateToChooseNext.length) {
            this.candidateToChooseNext[placement.size()] = 0;
        }
    }


    public interface DeletionFunction {

        void run(int deleted);

    }
}
