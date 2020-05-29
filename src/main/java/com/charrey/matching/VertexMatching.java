package com.charrey.matching;

import com.charrey.Occupation;
import com.charrey.Stateable;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.checker.DomainCheckerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VertexMatching extends VertexBlocker implements Stateable {


    private final ArrayList<Vertex> placement = new ArrayList<>();
    private final Vertex[][] candidates;          //for each candidate vertex i, candidates[i] lists all its compatible target vertices.
    private final int[] candidateToChooseNext;    //for each candidate vertex i, lists what target vertex to choose next.
    private final Occupation occupation;
    private DeletionFunction onDeletion;


    public VertexMatching(UtilityData data, MyGraph pattern, Occupation occupation) {
        this.candidates = data.getCompatibility();
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
        assert occupation.domainChecker.checkOK(placement.size());
        assert canPlaceNext();
        while (candidateToChooseNext[placement.size()] >= candidates[placement.size()].length) {
            removeLast();
        }
        assert occupation.domainChecker.checkOK(placement.size());
        Vertex toAdd = candidates[placement.size()][candidateToChooseNext[placement.size()]];
        boolean occupied = occupation.isOccupied(toAdd);
        assert occupation.domainChecker.checkOK(placement.size());
        if (occupied) {
            candidateToChooseNext[placement.size()] += 1;
            if (canPlaceNext()) {
                placeNext();
            }
        } else {
            String previousString = null;
            try {
                assert occupation.domainChecker.checkOK(placement.size());
                previousString = occupation.domainChecker.toString();
                assert occupation.domainChecker.checkOK(placement.size());
                occupation.occupyVertex(placement.size() + 1, toAdd);
                assert occupation.domainChecker.checkOK(placement.size() + 1);
                placement.add(toAdd);
            } catch (DomainCheckerException e) {
                assertEquals(previousString, occupation.domainChecker.toString());
                assert occupation.domainChecker.checkOK(placement.size());
                candidateToChooseNext[placement.size()] += 1;
                if (canPlaceNext()) {
                    placeNext();
                    assert occupation.domainChecker.checkOK(placement.size());
                    return;
                } else {
                    return;
                }
            }
        }
        assert occupation.domainChecker.checkOK(placement.size());
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
        assert occupation.domainChecker.checkOK(placement.size());
        if (placement.size() < candidateToChooseNext.length) {
            candidateToChooseNext[placement.size()] = 0;
        }
        Vertex removed = placement.remove(placement.size()-1);
        occupation.releaseVertex(placement.size(), removed);
        this.onDeletion.run(removed);
        candidateToChooseNext[placement.size()] += 1;
        assert occupation.domainChecker.checkOK(placement.size());
    }

    public void setOnDeletion(DeletionFunction x) {
        this.onDeletion = x;
    }

    public void giveAllowance() {
        if (placement.size() < candidateToChooseNext.length) {
            this.candidateToChooseNext[placement.size()] = 0;
        }
    }

    @Override
    public Object getState() {
        return new Object[]{placement, candidateToChooseNext};
    }


    public interface DeletionFunction {

        void run(Vertex deleted);

    }
}
