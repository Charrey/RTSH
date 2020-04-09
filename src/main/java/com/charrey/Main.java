package com.charrey;

import com.charrey.exceptions.NoSuchPairException;
import com.charrey.graph.Vertex;
import com.charrey.matching.NoMorePathsResult;
import com.charrey.matching.SuccessPathResult;
import com.charrey.matching.matchResults.edge.EdgeMatchResult;
import com.charrey.matching.matchResults.vertex.VertexMatchResult;
import com.charrey.matching.matchResults.vertex.OccupiedMatchResult;
import com.charrey.matching.matchResults.vertex.SuccessMatchResult;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
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

        UtilityData data = new UtilityData(pattern.getGraph(), target.getGraph());
        System.out.println(target);
        VertexMatching vertexMatching = new VertexMatching(data, pattern, target);
        EdgeMatching edgeMatching = new EdgeMatching(vertexMatching, data, pattern, target);

        try {
            Pair<Integer, Vertex> pairToTry = vertexMatching.explore();
            while (vertexMatching.hasNext() || edgeMatching.hasUnmatched()) {
                if (edgeMatching.hasUnmatched()) {


                    EdgeMatchResult matchResult = edgeMatching.tryNext();
                    if (matchResult instanceof SuccessPathResult && ((SuccessPathResult) matchResult).getPath().intermediate().stream().noneMatch(vertexMatching::blocks)) {
                        System.out.println("Celebrate");
                        edgeMatching.update((SuccessPathResult)matchResult);
                    } else {
                        System.out.println("Cry");
                        pairToTry = vertexMatching.retry(pairToTry);
                        //Todo: release bound edges;
                        //todo: subscribe to each other's blocked function
                    }


                } else {
                    VertexMatchResult matchResult = vertexMatching.tryNext(pairToTry);
                    if (matchResult instanceof SuccessMatchResult) {
                        vertexMatching.update(((SuccessMatchResult) matchResult));
                        pairToTry = vertexMatching.explore();
                    } else if (matchResult instanceof OccupiedMatchResult) {
                        pairToTry = vertexMatching.retry(pairToTry);
                    }
                }
            }
            System.out.println("Found homeomorphism: " + vertexMatching + "\n" + edgeMatching);
        }
        catch (NoSuchPairException e) {
            System.out.println("No homeomorphism.");
        }
    }




}
