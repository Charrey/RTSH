package com.charrey;

import com.charrey.algorithms.CompatibilityChecker;
import com.charrey.algorithms.GreatestConstrainedFirst;
import com.charrey.graph.AttributedVertex;
import com.charrey.graph.Path;
import com.charrey.heuristics.CarefulKitten;
import com.charrey.matchResults.*;
import com.charrey.router.LockTable;
import com.charrey.router.Router;
import com.charrey.util.GraphUtil;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

import static com.charrey.example.GraphGenerator.*;


public class Main {


    public static void main(String[] args) {
        GraphGeneration pattern = getPattern();
        GraphGeneration target = getTarget();

        Graph<AttributedVertex, DefaultEdge> targetGraph = target.graph;
        Graph<AttributedVertex, DefaultEdge> patternGraph = pattern.graph;

        List<AttributedVertex> order = new GreatestConstrainedFirst().apply(patternGraph);
        Map<AttributedVertex, Set<AttributedVertex>> compatibility = CompatibilityChecker.get(patternGraph, targetGraph);
        System.out.println(compatibility);
        Cache cache = new Cache(patternGraph, targetGraph, AttributedVertex::intData, new CarefulKitten());
        Map<AttributedVertex, AttributedVertex>[] toTryNext = GraphUtil.getToTryNext(order, compatibility, targetGraph);

        Router router = new Router();
        Set<AttributedVertex> occupation = new HashSet<>();

        State state = null;
        Cache.CacheEntry currentExpansion = null;

        while (state == null || state.numberOfMatchedVertices() < patternGraph.vertexSet().size()) {
            //explore freebies
            Cache.CacheEntry toExplore = cache.nextExploration();
            state = new State(patternGraph, order, targetGraph, toExplore.placement, toTryNext, occupation, router, target.routingTable, new LockTable());
            Pair<Integer, AttributedVertex> pairToTry = state.getNextPairExplore();
            while (state.hasNext()) {
                System.out.println(state);
                MatchResult matchResult = state.tryNext(pairToTry);
                if (matchResult instanceof SuccessMatchResult) {
                    System.out.println("Success match " + pairToTry.getFirst() + "--" + pairToTry.getSecond().getData());
                    state = ((SuccessMatchResult)matchResult).getState();
                    pairToTry = state.getNextPairExplore();
                } else if (matchResult instanceof RoutingFailedMatchResult) {
                    System.out.println("Routing failed for " + pairToTry.getFirst() + "--" + pairToTry.getSecond().getData());
                    pairToTry = state.getNextPairRetry(pairToTry);
                } else if (matchResult instanceof OccupiedMatchResult) {
                    System.out.println(pairToTry.getFirst() + "--" + pairToTry.getSecond().getData() + " is occupied");
                    pairToTry = state.getNextPairRetry(pairToTry);
                } else if (matchResult instanceof SuccessWithExtraVertices) {
                    SuccessWithExtraVertices semiSuccess = ((SuccessWithExtraVertices)matchResult);
                    List<AttributedVertex> placement = new LinkedList<>(state.getPlacement());
                    placement.add(semiSuccess.getPlacedVertex());
                    cache.add(placement, state.getParent(), semiSuccess.getAddedPaths(), semiSuccess.hasWorsePaths()); //todo: add parent;
                    pairToTry = state.getNextPairRetry(pairToTry);
                    System.out.println(pairToTry.getFirst() + "--" + pairToTry.getSecond().getData() + " requires extra vertices");

                }
                if (pairToTry == null) {
                    throw new RuntimeException("No isomorphism found.");
                }
            }
        }
    }




}
