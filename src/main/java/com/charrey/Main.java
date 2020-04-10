package com.charrey;

import com.charrey.graph.Vertex;
import com.charrey.matching.matchResults.edge.SuccessPathResult;
import com.charrey.matching.matchResults.edge.EdgeMatchResult;
import com.charrey.matching.matchResults.vertex.VertexMatchResult;
import com.charrey.matching.matchResults.vertex.SuccessMatchResult;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.util.UtilityData;
import org.jgrapht.alg.util.Pair;

import java.util.*;
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
        vertexMatching.include(edgeMatching);
        edgeMatching.include(vertexMatching);
        Deque<Object> history = new ArrayDeque<>();
        Pair<Integer, Vertex> pairToTry;
        while (vertexMatching.hasNext() || edgeMatching.hasUnmatched()) {
            System.out.println(history);
            if (!history.isEmpty() && history.peekFirst().equals(new SuccessPathResult(null))) {
                edgeMatching.removePath(null);
                history.removeFirst();
                if (history.peekFirst() instanceof SuccessPathResult) {
                    edgeMatching.removePath(((SuccessPathResult)history.removeFirst()).getPath());
                } else {
                    vertexMatching.remove((SuccessMatchResult)history.removeFirst());
                }
                System.out.println(history.peekFirst());
            }

            if (edgeMatching.hasUnmatched()) {
                EdgeMatchResult matchResult = edgeMatching.tryNext();
                edgeMatching.update((SuccessPathResult)matchResult);
                history.push(matchResult);
            } else {
                pairToTry = vertexMatching.explore();
                VertexMatchResult matchResult = vertexMatching.tryNext(pairToTry);
                vertexMatching.update(((SuccessMatchResult) matchResult));
                history.addFirst(matchResult);
            }
            edgeMatching.synchronize();
        }
        System.out.println("Found homeomorphism: " + vertexMatching + "\n" + edgeMatching);
    }




}
