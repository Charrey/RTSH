package com.charrey.matching;

import com.charrey.Occupation;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.Vertex;
import com.charrey.util.UtilityData;

import java.util.*;

public class VertexMatching extends VertexBlocker {


    private final LinkedList<Vertex> placement = new LinkedList<>();
    private final Vertex[][] candidates;          //for each candidate vertex i, candidates[i] lists all its compatible target vertices.
    private final int[] candidateToChooseNext;    //for each candidate vertex i, lists what target vertex to choose next.
    private DeletionFunction onDeletion;


    public VertexMatching(UtilityData data, GraphGeneration pattern) {
        this.candidates = data.getCompatibility();
        candidateToChooseNext = new int[pattern.getGraph().vertexSet().size()];
        assert candidateToChooseNext.length == candidates.length;
        Arrays.fill(candidateToChooseNext, 0);
    }

    public boolean canPlaceNext() {
        if (placement.size() >= candidates.length) {
            return false;
        } else {
            return !(candidateToChooseNext[placement.size()] >= candidates[placement.size()].length);
        }
    }

    public VertexMatching placeNext() {
        assert canPlaceNext();
        while (candidateToChooseNext[placement.size()] >= candidates[placement.size()].length) {
            candidateToChooseNext[placement.size()] = 0;
            Vertex removed = placement.removeLast();
            Occupation.getOccupation(removed.getGraph()).release(removed);
            candidateToChooseNext[placement.size()] += 1;
            try {
                assert canPlaceNext();
            } catch (AssertionError e) {
                throw e;
            }
            this.onDeletion.run(this);
        }
        Vertex toAdd = candidates[placement.size()][candidateToChooseNext[placement.size()]];
        boolean occupied = Occupation.getOccupation(toAdd.getGraph()).isOccupied(toAdd);
        if (occupied) {
            candidateToChooseNext[placement.size()] += 1;
            if (canPlaceNext()) {
                return placeNext();
            } else {
                return null;
            }
        } else {
            placement.add(toAdd);
        }
        Occupation.getOccupation(toAdd.getGraph()).occupy(toAdd);
        return this;
    }



    public List<Vertex> getPlacement() {
        return Collections.unmodifiableList(placement);
    }


    @Override
    public String toString() {
        return "State {\n" + "\tplacement:\t" + placement + "\n" +
                "}";
    }

    public boolean canRetry() {
        return placement.size() > 0;
    }

    public void retry() {
        if (placement.size() < candidateToChooseNext.length) {
            candidateToChooseNext[placement.size()] = 0;
        }
        Vertex removed = placement.remove(placement.size()-1);
        Occupation.getOccupation(removed.getGraph()).release(removed);
        candidateToChooseNext[placement.size()] += 1;
    }

    public void setOnDeletion(DeletionFunction x) {
        this.onDeletion = x;
    }

    public void giveAllowance() {
        if (placement.size() < candidateToChooseNext.length) {
            this.candidateToChooseNext[placement.size()] = 0;
        }
    }



    public interface DeletionFunction {

        void run(VertexMatching vMatching);

    }
}
