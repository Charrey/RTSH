package com.charrey;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.util.*;
import org.jgrapht.alg.util.Pair;

import java.util.logging.Logger;

import static com.charrey.example.GraphGenerator.*;


public class Main {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        GraphGeneration pattern = getPattern();
        GraphGeneration target = getTarget();


        System.out.println(pattern);
        System.out.println(target);

        UtilityData data = new UtilityData(pattern.getGraph(), target.getGraph());
        VertexMatching vertexMatching = new VertexMatching(data, pattern);
        EdgeMatching edgeMatching = new EdgeMatching(vertexMatching, data, pattern, target);
        boolean exhausedAllPaths = false;
        while (vertexMatching.hasNext() || edgeMatching.hasUnmatched() || !Util.conflicted(vertexMatching, edgeMatching).ok) {
            System.out.println(vertexMatching);
            System.out.println(edgeMatching);
            ConflictReport report = Util.conflicted(vertexMatching, edgeMatching);
            while (exhausedAllPaths || !report.ok) {
                if (exhausedAllPaths) {
                    exhausedAllPaths = false;
                    applySolution(vertexMatching, edgeMatching, Solution.RETRY_LAST_VERTEX);
                } else {
                    applySolution(vertexMatching, edgeMatching, report.solution);
                }
                report = Util.conflicted(vertexMatching, edgeMatching);
            }

            if (edgeMatching.hasUnmatched()) {
                Path nextpath = edgeMatching.next();
                if (nextpath == null) {
                    exhausedAllPaths = true;
                }
            } else {
                vertexMatching.next();
                edgeMatching.synchronize();
            }
        }
        System.out.println("Found homeomorphism: " + vertexMatching + "\n" + edgeMatching);
    }

    private static void applySolution(VertexMatching vertexMatching, EdgeMatching edgeMatching, Solution solution) {
        switch (solution) {
            case RETRY_LAST_VERTEX:
                vertexMatching.retry();
                edgeMatching.synchronize();
                break;
            case RETRY_LAST_PATH:
                edgeMatching.retry();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        System.out.println();
    }


}
