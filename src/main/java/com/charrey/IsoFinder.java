package com.charrey;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.RandomTestCaseGenerator;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.util.Util;
import com.charrey.util.UtilityData;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;


public class IsoFinder {

    private static final Logger LOG = Logger.getLogger("IsoFinder");

    public static Optional<Homeomorphism> getHomeomorphism(RandomTestCaseGenerator.TestCase testcase) {
        BigInteger naiveVertexDomainSize = new BigInteger(String.valueOf(testcase.source.getGraph().vertexSet().size())).pow(testcase.target.getGraph().vertexSet().size());
        UtilityData data = new UtilityData(testcase.source.getGraph(), testcase.target.getGraph());
        BigInteger vertexDomainSize = Arrays.stream(data.getCompatibility()).reduce(new BigInteger("1"), (i, vs) -> i.multiply(new BigInteger(String.valueOf(vs.length))), BigInteger::multiply);
        LOG.info(() -> "Reduced domain by a factor of " + (naiveVertexDomainSize.doubleValue() / vertexDomainSize.doubleValue()) + " to " + vertexDomainSize);

        if (Arrays.stream(data.getCompatibility()).anyMatch(x -> x.length == 0)) {
            return Optional.empty();
        }
        Occupation occupation         = new Occupation(data, testcase.target.getGraph().vertexSet().size());
        VertexMatching vertexMatching = new VertexMatching(data, testcase.source, occupation);
        EdgeMatching edgeMatching     = new EdgeMatching(vertexMatching, data, testcase.source, testcase.target, occupation);
        boolean exhausedAllPaths = false;

        long iterations = 0;
        while (!allDone(testcase.source.getGraph(), testcase.target.getGraph(), vertexMatching, edgeMatching)) {
            iterations++;
            LOG.fine(vertexMatching::toString);
            LOG.fine(edgeMatching::toString);
            if (exhausedAllPaths) {
                exhausedAllPaths = false;
                vertexMatching.removeLast();
                continue;
            }

            if (edgeMatching.hasUnmatched()) {
                Path nextpath = edgeMatching.placeNextUnmatched();
                if (nextpath == null) {
                    exhausedAllPaths = true;
                }
            } else if (vertexMatching.canPlaceNext()) {
                vertexMatching.placeNext();
            } else if (edgeMatching.retry()) {
                vertexMatching.giveAllowance();
            } else if (vertexMatching.canRetry()) {
                vertexMatching.removeLast();
            } else {
                return Optional.empty();
            }
        }
        if (vertexMatching.getPlacementUnsafe().size() < testcase.source.getGraph().vertexSet().size()) {
            return Optional.empty();
        } else {
            return Optional.of(new Homeomorphism(vertexMatching, edgeMatching, iterations));
        }
    }



    private static boolean allDone(Graph<Vertex, DefaultEdge> pattern, Graph<Vertex, DefaultEdge> target, VertexMatching vertexMatching, EdgeMatching edgeMatching) {
        boolean completeV = vertexMatching.getPlacementUnsafe().size() == pattern.vertexSet().size();
        if (!completeV) {
            return false;
        }
        boolean completeE = !edgeMatching.hasUnmatched();
        if (!completeE) {
            return false;
        }
        LOG.info(() -> "Done, checking...");
        assert  Util.isCorrect(pattern, target, vertexMatching, edgeMatching);
        return true;
    }


}
