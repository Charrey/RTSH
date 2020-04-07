package com.charrey;

import com.charrey.exceptions.FullHomeomorphismFound;
import com.charrey.exceptions.NoSuchPairException;
import com.charrey.graph.Vertex;
import com.charrey.matchResults.MatchResult;
import com.charrey.matchResults.OccupiedMatchResult;
import com.charrey.matchResults.SuccessMatchResult;
import com.charrey.util.UtilityData;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import java.util.logging.Logger;

import static com.charrey.example.GraphGenerator.*;


public class Main {

    private final static Logger LOGGER = Logger.getLogger("Main");


    public static void main(String[] args) {
        GraphGeneration pattern = getPattern();
        GraphGeneration target = getTarget();

        Graph<Vertex, DefaultEdge> targetGraph = target.getGraph();
        Graph<Vertex, DefaultEdge> patternGraph = pattern.getGraph();

        UtilityData utilityData = new UtilityData(patternGraph, targetGraph);

        State state = new State(pattern, target);
        try {
            Pair<Integer, Vertex> pairToTry = state.explore();
            while (state.hasNext()) {
                MatchResult matchResult = state.tryNext(pairToTry);
                if (matchResult instanceof SuccessMatchResult) {
                    state.update(((SuccessMatchResult) matchResult));
                    pairToTry = state.explore();
                } else if (matchResult instanceof OccupiedMatchResult) {
                    pairToTry = state.retry(pairToTry);
                }
            }

        } catch (FullHomeomorphismFound e) {
            System.out.println("Found homeomorphism: " + state);
        } catch (NoSuchPairException e) {
            System.out.println("No homeomorphism.");
        }
    }




}
