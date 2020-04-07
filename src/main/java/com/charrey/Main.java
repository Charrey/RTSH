package com.charrey;

import com.charrey.algorithms.CompatibilityChecker;
import com.charrey.algorithms.GreatestConstrainedFirst;
import com.charrey.exceptions.FullyExploredException;
import com.charrey.graph.Vertex;
import com.charrey.heuristics.BestFirstSearch;
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

        Graph<Vertex, DefaultEdge> targetGraph = target.graph;
        Graph<Vertex, DefaultEdge> patternGraph = pattern.graph;

        List<Vertex> order = new GreatestConstrainedFirst().apply(patternGraph);
        Map<Vertex, Set<Vertex>> compatibility = CompatibilityChecker.get(patternGraph, targetGraph);
        Cache cache = new Cache(patternGraph, targetGraph, Vertex::intData, new BestFirstSearch());
        Map<Vertex, Vertex>[] toTryNext = GraphUtil.getToTryNext(order, compatibility, targetGraph);

        Router router = new Router();
        Set<Vertex> occupation = new HashSet<>();

        State state = null;
        try {
            while (state == null || state.numberOfMatchedVertices() < patternGraph.vertexSet().size()) {
                //explore freebies
                Cache.CacheEntry toExplore = cache.nextExploration();
                state = new State(patternGraph, order, targetGraph, toExplore.placement, toTryNext, occupation, router, target.routingTable, new LockTable());
                Pair<Integer, Vertex> pairToTry = state.getNextPairExplore();
                while (state.hasNext()) {
                    //System.out.println(state);
                    MatchResult matchResult = state.tryNext(pairToTry);
                    if (matchResult instanceof SuccessMatchResult) {
                        //System.out.println("Success match " + pairToTry.getFirst() + "--" + pairToTry.getSecond().getData());
                        state = ((SuccessMatchResult) matchResult).getState();
                        pairToTry = state.getNextPairExplore();
                    } else if (matchResult instanceof OccupiedMatchResult) {
                        //System.out.println(pairToTry.getFirst() + "--" + pairToTry.getSecond().getData() + " is occupied");
                        pairToTry = state.getNextPairRetry(pairToTry);
                    }
                    if (pairToTry == null) {
                        throw new RuntimeException("No isomorphism found.");
                    }
                }
            }
        } catch (FullyExploredException e) {
            System.out.println("Found homeomorphism: " + state);
        }
    }




}
