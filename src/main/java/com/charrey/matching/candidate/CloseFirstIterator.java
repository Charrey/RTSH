package com.charrey.matching.candidate;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.ArrayUtils;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.IntVertexDijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.*;

public class CloseFirstIterator extends VertexCandidateIterator {

    private final VertexMatching vertexMatching;
    private final boolean cached;

    private int lastReturned = ABSENT;
    private double lastDistance = 0d;

    private TIntList candidatesSorted;
    private int nextIndexToPrepare = 0;

    CloseFirstIterator(MyGraph sourceGraph,
                       MyGraph targetGraph,
                       Settings settings,
                       GlobalOccupation occupation,
                       int sourceGraphVertex,
                       VertexMatching vertexMatching,
                       boolean cached) {
        super(sourceGraph, targetGraph, settings, occupation, sourceGraphVertex);
        this.vertexMatching = vertexMatching;
        reset();
        this.cached = cached;
    }

    private void populateCandidates() {
        TIntSet matchedTargetVertices = new TIntHashSet(Graphs.neighborSetOf(sourceGraph, sourceGraphVertex).stream().filter(x -> x < vertexMatching.get().size()).mapToInt(x -> vertexMatching.get().get(x)).toArray());
        Iterator<Integer> it = getInnerIterator();
        ShortestPathAlgorithm<Integer, MyEdge> shortestPathAlgorithm = new IntVertexDijkstraShortestPath<>(targetGraph.isDirected() ? new AsUndirectedGraph<>(targetGraph) : targetGraph);
        TIntDoubleMap distances = new TIntDoubleHashMap();
        while (it.hasNext()) {
            int candidate = it.next();
            Optional<Double> candidateDistance2 = getCandidateDistance(candidate, Double.MAX_VALUE, matchedTargetVertices, shortestPathAlgorithm);
            if (candidateDistance2.isPresent()) { //otherwise it is not a valid candidate
                double actualDistance = candidateDistance2.get();
                distances.put(candidate, actualDistance);
            }
        }
        Integer[] keyObjects = ArrayUtils.toObject(distances.keys());
        Arrays.sort(keyObjects, Comparator.comparingDouble(distances::get));
        candidatesSorted = new TIntArrayList(ArrayUtils.toPrimitive(keyObjects));
    }

    @Override
    public void doReset() {
        this.nextToReturn = ABSENT;
        lastDistance = 0d;
        nextIndexToPrepare = 0;
    }

    private static Optional<Double> getCandidateDistance(int candidate, double bestDistanceSoFar, TIntSet matchedTargetVertices, ShortestPathAlgorithm<Integer, MyEdge> shortestPathAlgorithm) {
        final double[] candidateDistance = {0};
        final boolean[] mayContinue = {true};
        matchedTargetVertices.forEach(value -> {
            GraphPath<Integer, MyEdge> path = shortestPathAlgorithm.getPath(value, candidate);
            if (path == null) {
                mayContinue[0] = false;
                return false;
            }
            candidateDistance[0] += path.getWeight();
            return candidateDistance[0] <= bestDistanceSoFar;
        });
        if (!mayContinue[0]) {
            return Optional.empty();
        }
        return Optional.of(candidateDistance[0]);
    }



    @Override
    protected void prepareNextToReturn() {
        if (cached) {
            if (nextIndexToPrepare == 0) {
                populateCandidates();
            }
            prepareNextToReturnCached();
        } else {
            prepareNextToReturnUncached();
        }

    }

    private void prepareNextToReturnCached() {
        if (nextIndexToPrepare >= candidatesSorted.size()) {
            nextToReturn = EXHAUSTED;
        } else {
            nextToReturn = candidatesSorted.get(nextIndexToPrepare);
            nextIndexToPrepare++;
        }
    }

    private void prepareNextToReturnUncached() {
        TIntSet matchedTargetVertices = new TIntHashSet(Graphs.neighborSetOf(sourceGraph, sourceGraphVertex).stream().filter(x -> x < vertexMatching.get().size()).mapToInt(x -> vertexMatching.get().get(x)).toArray());
        int bestNewCandidate = Integer.MAX_VALUE;
        double bestNewDistance = Double.POSITIVE_INFINITY;

        Iterator<Integer> it = getInnerIterator();
        ShortestPathAlgorithm<Integer, MyEdge> shortestPathAlgorithm = new IntVertexDijkstraShortestPath<>(targetGraph.isDirected() ? new AsUndirectedGraph<>(targetGraph) : targetGraph);

        while (it.hasNext()) {
            int candidate = it.next();

            Optional<Double> candidateDistance2 = getCandidateDistance(candidate, bestNewDistance, matchedTargetVertices, shortestPathAlgorithm);
            if (candidateDistance2.isPresent()) { //otherwise it is not a valid candidate
                double actualDistance = candidateDistance2.get();
                if ((actualDistance > lastDistance && actualDistance < bestNewDistance) ||
                        (actualDistance == lastDistance && candidate > lastReturned && (lastDistance < bestNewDistance || candidate < bestNewCandidate)) ||
                        (actualDistance == bestNewDistance && candidate < bestNewCandidate)) {
                    bestNewCandidate = candidate;
                    bestNewDistance = actualDistance;
                }
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
