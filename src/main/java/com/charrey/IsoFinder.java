package com.charrey;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.util.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;


public class IsoFinder {

    private static final Logger LOG = Logger.getLogger("IsoFinder");

    public static Optional<Homeomorphism> getHomeomorphism(GraphGeneration pattern, GraphGeneration target) {
        UtilityData data = new UtilityData(pattern.getGraph(), target.getGraph());
        if (Arrays.stream(data.getCompatibility()).anyMatch(x -> x.length == 0)) {
            return Optional.empty();
        }
        VertexMatching vertexMatching = new VertexMatching(data, pattern);
        EdgeMatching edgeMatching     = new EdgeMatching(vertexMatching, data, pattern, target);
        Occupation occupation         = Occupation.getOccupation(target.getGraph());
        boolean exhausedAllPaths = false;
        while (!allDone(pattern.getGraph(), vertexMatching, edgeMatching)) {
            DOTViewer.printIfNecessary(pattern.getGraph(), target.getGraph(), vertexMatching, edgeMatching);
            LOG.fine(vertexMatching::toString);
            LOG.fine(edgeMatching::toString);
            ConflictReport report = Util.conflicted(vertexMatching, edgeMatching);
            if (exhausedAllPaths || !report.ok) {
                if (exhausedAllPaths) {
                    exhausedAllPaths = false;
                    applySolution(vertexMatching, edgeMatching, Solution.RETRY_LAST_VERTEX);
                } else {
                    applySolution(vertexMatching, edgeMatching, report.solution);
                }
                continue;
            }

            if (edgeMatching.hasUnmatched()) {
                Path nextpath = edgeMatching.placeNextUnmatched();
                if (nextpath == null) {
                    exhausedAllPaths = true;
                }
            } else if (vertexMatching.canPlaceNext()) {
                vertexMatching.placeNext();
                edgeMatching.synchronize();
            } else if (edgeMatching.canRetry()) {
                edgeMatching.retry();
                vertexMatching.giveAllowance();
            } else if (vertexMatching.canRetry()) {
                vertexMatching.retry();
                edgeMatching.synchronize();
            } else {
                return Optional.empty();
            }
        }
        occupation.release(target.getGraph());
        if (vertexMatching.getPlacement().size() < pattern.getGraph().vertexSet().size()) {
            return Optional.empty();
        } else {
            //System.out.println(iterations);
            return Optional.of(new Homeomorphism(vertexMatching, edgeMatching));
        }
    }

    private static boolean allDone(Graph<Vertex, DefaultEdge> pattern, VertexMatching vertexMatching, EdgeMatching edgeMatching) {
        boolean completeV = vertexMatching.getPlacement().size() == pattern.vertexSet().size();
        boolean completeE = !edgeMatching.hasUnmatched();
        boolean correct = Util.conflicted(vertexMatching, edgeMatching).ok;
        return completeV && completeE && correct;
    }

    private static void applySolution(VertexMatching vertexMatching, EdgeMatching edgeMatching, Solution solution) {
        switch (solution) {
            case RETRY_LAST_VERTEX:
                if (vertexMatching.canRetry()) {
                    vertexMatching.retry();
                    edgeMatching.synchronize();
                } else {
                    System.exit(-2);
                }
                break;
            case RETRY_LAST_PATH:
                if (edgeMatching.canRetry()) {
                    edgeMatching.retry(); //this can fail. In this case we should just remove the last path.
                    vertexMatching.giveAllowance();
                } else {
                    edgeMatching.removeLastPath();
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }


}
