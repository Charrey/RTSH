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
        GraphGeneration<AttributedVertex<Integer>> pattern = getPattern();
        GraphGeneration<AttributedVertex<Character>> target = getTarget();
        Graph<AttributedVertex<Character>, DefaultEdge> targetGraph = target.graph;
        Graph<AttributedVertex<Integer>, DefaultEdge> patternGraph = pattern.graph;
        List<AttributedVertex<Integer>> order = new GreatestConstrainedFirst<AttributedVertex<Integer>, DefaultEdge>().apply(patternGraph);
        Map<AttributedVertex<Integer>, Set<AttributedVertex<Character>>> compatibility = CompatibilityChecker.get(patternGraph, targetGraph);
        System.out.println(compatibility);
        Cache<AttributedVertex<Integer>, AttributedVertex<Character>> cache = new Cache<>(patternGraph, targetGraph, x -> x.id, new CarefulKitten<>());
        Map<AttributedVertex<Character>, AttributedVertex<Character>>[] toTryNext = GraphUtil.getToTryNext(order, compatibility, targetGraph);

        Router<AttributedVertex<Character>> router = new Router<>();
        Set<AttributedVertex<Character>> occupation = new HashSet<>();

        State<AttributedVertex<Character>> state = null;
        Cache.CacheEntry<AttributedVertex<Integer>, AttributedVertex<Character>> currentExpansion = null;

        while (state == null || state.numberOfMatchedVertices() < patternGraph.vertexSet().size()) {
            //explore freebies
            Cache.CacheEntry<AttributedVertex<Integer>, AttributedVertex<Character>> toExplore = cache.nextExploration();
            state = new State<>(patternGraph, order, targetGraph, toExplore.placement, toTryNext, occupation, router, target.routingTable, new LockTable<>());
            Pair<Integer, AttributedVertex<Character>> pairToTry = state.getNextPairExplore();
            while (state.hasNext()) {
                System.out.println(state);
                MatchResult<AttributedVertex<Integer>, AttributedVertex<Character>> matchResult = state.tryNext(pairToTry);
                if (matchResult instanceof SuccessMatchResult) {
                    System.out.println("Success match " + pairToTry.getFirst() + "--" + pairToTry.getSecond().id);
                    state = ((SuccessMatchResult<AttributedVertex<Integer>, AttributedVertex<Character>>)matchResult).getState();
                    pairToTry = state.getNextPairExplore();
                } else if (matchResult instanceof RoutingFailedMatchResult) {
                    System.out.println("Routing failed for " + pairToTry.getFirst() + "--" + pairToTry.getSecond().id);
                    pairToTry = state.getNextPairRetry(pairToTry);
                } else if (matchResult instanceof OccupiedMatchResult) {
                    System.out.println(pairToTry.getFirst() + "--" + pairToTry.getSecond().id + " is occupied");
                    pairToTry = state.getNextPairRetry(pairToTry);
                } else if (matchResult instanceof SuccessWithExtraVertices) {
                    SuccessWithExtraVertices<AttributedVertex<Integer>, AttributedVertex<Character>> semiSuccess = ((SuccessWithExtraVertices<AttributedVertex<Integer>, AttributedVertex<Character>>)matchResult);
                    List<AttributedVertex<Character>> placement = new LinkedList<>(state.getPlacement());
                    placement.add(semiSuccess.getPlacedVertex());
                    cache.add(placement, state.getParent(), semiSuccess.getAddedPaths(), semiSuccess.hasWorsePaths()); //todo: add parent;
                    pairToTry = state.getNextPairRetry(pairToTry);
                    System.out.println(pairToTry.getFirst() + "--" + pairToTry.getSecond().id + " requires extra vertices");

                }
                if (pairToTry == null) {
                    throw new RuntimeException("No isomorphism found.");
                }
            }
        }
    }




}
