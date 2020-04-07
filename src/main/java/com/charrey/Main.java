package com.charrey;

import com.charrey.algorithms.CompatibilityChecker;
import com.charrey.algorithms.GreatestConstrainedFirst;
import com.charrey.exceptions.FullHomeomorphismFound;
import com.charrey.exceptions.NoSuchPairException;
import com.charrey.graph.Vertex;
import com.charrey.heuristics.BestFirstSearch;
import com.charrey.matchResults.*;
import com.charrey.router.LockTable;
import com.charrey.router.Router;
import com.charrey.util.GraphUtil;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import javax.sound.midi.Soundbank;
import java.util.*;
import java.util.logging.Logger;

import static com.charrey.example.GraphGenerator.*;


public class Main {

    private final static Logger LOGGER= Logger.getLogger("Main");


    public static void main(String[] args) {
        GraphGeneration pattern = getPattern();
        GraphGeneration target = getTarget();

        Graph<Vertex, DefaultEdge> targetGraph = target.graph;
        Graph<Vertex, DefaultEdge> patternGraph = pattern.graph;

        List<Vertex> order = new GreatestConstrainedFirst().apply(patternGraph);
        Map<Vertex, Set<Vertex>> compatibility = CompatibilityChecker.get(patternGraph, targetGraph);
        Map<Vertex, Vertex>[] toTryNext = GraphUtil.getToTryNext(order, compatibility, targetGraph);

        Router router = new Router();
        Set<Vertex> occupation = new HashSet<>();

        State state = null;
        try {
            state = new State(patternGraph, order, targetGraph, new LinkedList<>(), toTryNext, occupation, router, target.routingTable, new LockTable());
            Pair<Integer, Vertex> pairToTry = state.getNextPairExplore();
            while (state.hasNext()) {
                MatchResult matchResult = state.tryNext(pairToTry);
                if (matchResult instanceof SuccessMatchResult) {
                    state.update(((SuccessMatchResult) matchResult));
                    pairToTry = state.getNextPairExplore();
                } else if (matchResult instanceof OccupiedMatchResult) {
                    pairToTry = state.getNextPairRetry(pairToTry);
                }
            }

        } catch (FullHomeomorphismFound e) {
            System.out.println("Found homeomorphism: " + state);
        } catch (NoSuchPairException e) {
            System.out.println("No homeomorphism.");
        }
    }




}
