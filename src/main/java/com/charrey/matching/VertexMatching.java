package com.charrey.matching;

import com.charrey.Occupation;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.Vertex;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.DomainChecker;

import java.util.*;

public class VertexMatching extends VertexBlocker {


    private final ArrayList<Vertex> placement = new ArrayList<>();
    private final Vertex[][] candidates;          //for each candidate vertex i, candidates[i] lists all its compatible target vertices.
    private final int[] candidateToChooseNext;    //for each candidate vertex i, lists what target vertex to choose next.
    private final Occupation occupation;
    private DeletionFunction onDeletion;


    public VertexMatching(UtilityData data, GraphGeneration pattern, Occupation occupation) {
        this.candidates = data.getCompatibility();
        candidateToChooseNext = new int[pattern.getGraph().vertexSet().size()];
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

    public VertexMatching placeNext() {
        assert canPlaceNext();
        while (candidateToChooseNext[placement.size()] >= candidates[placement.size()].length) {
            candidateToChooseNext[placement.size()] = 0;
            Vertex removed = placement.remove(placement.size()-1);
            occupation.releaseVertex(placement.size(), removed);
            this.onDeletion.run(removed);
            candidateToChooseNext[placement.size()] += 1;
            assert canPlaceNext();

        }
        Vertex toAdd = candidates[placement.size()][candidateToChooseNext[placement.size()]];
        boolean occupied = occupation.isOccupied(toAdd);
        if (occupied) {
            candidateToChooseNext[placement.size()] += 1;
            if (canPlaceNext()) {
                return placeNext();
            } else {
                return null;
            }
        } else {
            try {
                occupation.occupyVertex(placement.size(), toAdd);
                placement.add(toAdd);
            } catch (DomainChecker.EmptyDomainException e) {
                candidateToChooseNext[placement.size()] += 1;
                if (canPlaceNext()) {
                    return placeNext();
                } else {
                    return null;
                }
            }
        }
        return this;
    }


    public List<Vertex> getPlacementUnsafe() {
        return placement;
    }


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
        Vertex removed = placement.remove(placement.size()-1);
        occupation.releaseVertex(placement.size(), removed);
        this.onDeletion.run(removed);
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

        void run(Vertex deleted);

    }
}
