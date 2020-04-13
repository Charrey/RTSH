package com.charrey.matching;

import com.charrey.example.GraphGenerator;
import com.charrey.graph.RoutingVertexTable;
import com.charrey.graph.Vertex;
import com.charrey.router.LockTable;
import com.charrey.util.UtilityData;
import org.jgrapht.alg.util.Pair;

import java.util.*;

public class VertexMatching extends VertexBlocker {


    private final LinkedList<Vertex> placement = new LinkedList<>();
    private final Vertex[][] candidates;          //for each candidate vertex i, candidates[i] lists all its compatible target vertices.
    private final int[] candidateToChooseNext;    //for each candidate vertex i, lists what target vertex to choose next.


    public VertexMatching(UtilityData data, GraphGenerator.GraphGeneration pattern) {
        this.candidates = data.getCompatibility();
        candidateToChooseNext = new int[pattern.getGraph().vertexSet().size()];
        assert candidateToChooseNext.length == candidates.length;
        Arrays.fill(candidateToChooseNext, 0);
    }

    private Pair<Integer, Vertex> cached;

    public boolean hasNext() {
        if (placement.size() >= candidates.length) {
            return false;
        } else {
            return candidateToChooseNext[placement.size()] < candidates[placement.size()].length;
        }
    }

    public void next() {
        while (candidateToChooseNext[placement.size()] >= candidates[placement.size()].length) {
            candidateToChooseNext[placement.size()] = 0;
            placement.removeLast();
            candidateToChooseNext[placement.size()] += 1;
        }
        Vertex toAdd = candidates[placement.size()][candidateToChooseNext[placement.size()]];
        placement.add(toAdd);
    }

    public List<Vertex> getPlacement() {
        return Collections.unmodifiableList(placement);
    }


    @Override
    public String toString() {
        return "State {\n" + "\tplacement:\t" + placement + "\n" +
                "}";
    }


    public boolean hasNextCandidate(Integer index) {
        return this.candidates[index].length > this.candidateToChooseNext[index];
    }

    public void retry() {
        if (placement.size() < candidateToChooseNext.length) {
            candidateToChooseNext[placement.size()] = 0;
        }
        Vertex removed = placement.remove(placement.size()-1);
        candidateToChooseNext[placement.size()] += 1;
    }

//    public Pair<Integer, Vertex> nextCandidate(Integer index) {
//        assert hasNextCandidate(index);
//        while (index >= placement.size()) {
//            placement.add(null);
//        }
//        assert index < candidates.length;
//        assert index < candidateToChooseNext.length;
//        assert candidateToChooseNext[index] < candidates[index].length;
//        this.placement.set(index, candidates[index][candidateToChooseNext[index]]);
//        this.candidateToChooseNext[index] += 1;
//        return new Pair<>(index, placement.get(index));
//    }

//    public void removeLast() {
//        candidateToChooseNext[placement.size()-1] += 1;
//        placement.removeLast();
//    }
}
