package com.charrey.matching.candidate;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.IntVertexDijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.Iterator;

public class CloseFirstIterator extends VertexCandidateIterator {

    private final VertexMatching vertexMatching;

    private int lastReturned = ABSENT;
    private double lastDistance = 0d;

    CloseFirstIterator(MyGraph sourceGraph, MyGraph targetGraph, Settings settings, GlobalOccupation occupation, int sourceGraphVertex, VertexMatching vertexMatching) {
        super(sourceGraph, targetGraph, settings, occupation, sourceGraphVertex);
        this.vertexMatching = vertexMatching;
        reset();
    }

    @Override
    public void doReset() {
        this.nextToReturn = ABSENT;
        lastDistance = 0d;
    }

    @Override
    protected void prepareNextToReturn() {

        TIntSet matchedTargetVertices = new TIntHashSet(Graphs.neighborSetOf(sourceGraph, sourceGraphVertex).stream().filter(x -> x < vertexMatching.getPlacement().size()).mapToInt(x -> vertexMatching.getPlacement().get(x)).toArray());


        int bestNewCandidate = Integer.MAX_VALUE;
        double bestNewDistance = Double.POSITIVE_INFINITY;

        Iterator<Integer> it = getInnerIterator();
        ShortestPathAlgorithm<Integer, MyEdge> shortestPathAlgorithm = new IntVertexDijkstraShortestPath<>(targetGraph.isDirected() ? new AsUndirectedGraph<>(targetGraph) : targetGraph);

        while (it.hasNext()) {
            int candidate = it.next();

            final double[] candidateDistance = {0};
            double finalBestNewDistance = bestNewDistance;
            final boolean[] mayContinue = {true};
            matchedTargetVertices.forEach(value -> {
                GraphPath<Integer, MyEdge> path = shortestPathAlgorithm.getPath(value, candidate);
                if (path == null) {
                    mayContinue[0] = false;
                    return false;
                }
                candidateDistance[0] += path.getWeight();
                return candidateDistance[0] <= finalBestNewDistance;
            });
            if (!mayContinue[0]) {
                continue;
            }
            if ((candidateDistance[0] > lastDistance && candidateDistance[0] < bestNewDistance) ||
                    (candidateDistance[0] == lastDistance && candidate > lastReturned && (lastDistance < bestNewDistance || candidate < bestNewCandidate)) ||
                    (candidateDistance[0] == bestNewDistance && candidate < bestNewCandidate)) {
                bestNewCandidate = candidate;
                bestNewDistance = candidateDistance[0];
            }
        }
        assert bestNewCandidate == ABSENT || bestNewCandidate != lastReturned;
        lastDistance = bestNewDistance;
        if (bestNewCandidate == Integer.MAX_VALUE) {
            lastReturned = EXHAUSTED;
            nextToReturn = EXHAUSTED;
        } else {
            lastReturned = bestNewCandidate;
            nextToReturn = bestNewCandidate;
        }
    }


}
