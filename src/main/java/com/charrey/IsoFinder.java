package com.charrey;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.TestCase;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.util.Util;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.checker.DomainCheckerException;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Logger;


public class IsoFinder {

    private static final Logger LOG = Logger.getLogger("IsoFinder");
    private static EdgeMatching edgeMatching;
    private static VertexMatching vertexMatching;
    private static Occupation occupation;
    private static UtilityData data;

    private static void setup(TestCase testcase) throws DomainCheckerException {
        data = new UtilityData(testcase.source.getGraph(), testcase.target.getGraph());
        logDomainReduction(testcase, data);
        if (Arrays.stream(data.getCompatibility()).anyMatch(x -> x.length == 0)) {
            throw new DomainCheckerException();
        }
        occupation         = new Occupation(data, testcase.target.getGraph().vertexSet().size());
        vertexMatching = new VertexMatching(data, testcase.source, occupation);
        edgeMatching     = new EdgeMatching(vertexMatching, data, testcase.source, testcase.target, occupation);
    }

    public static HomeomorphismResult getHomeomorphism(TestCase testcase) {
        try {
            setup(testcase);
        } catch (DomainCheckerException e) {
            return HomeomorphismResult.COMPATIBILITY_FAIL;
        }
        long iterations = 0;
        while (!allDone(testcase.source.getGraph(), vertexMatching, edgeMatching)) {
            iterations++;
            LOG.fine(vertexMatching::toString);
            LOG.fine(edgeMatching::toString);
            if (edgeMatching.hasUnmatched()) {
                Path nextpath = edgeMatching.placeNextUnmatched();
                if (nextpath == null) {
                    if (edgeMatching.retry()) {
                        vertexMatching.giveAllowance();
                    } else {
                        vertexMatching.removeLast();
                    }
                }
            } else if (vertexMatching.canPlaceNext()) {
                vertexMatching.placeNext();
            } else if (edgeMatching.retry()) {
                vertexMatching.giveAllowance();
            } else if (vertexMatching.canRetry()) {
                vertexMatching.removeLast();
            } else {
                return HomeomorphismResult.ofFailed(iterations);
            }
        }
        if (vertexMatching.getPlacementUnsafe().size() < testcase.source.getGraph().vertexSet().size()) {
            return HomeomorphismResult.ofFailed(iterations);
        } else {
            return HomeomorphismResult.ofSucceed(vertexMatching, edgeMatching, iterations);
        }
    }



    private static void logDomainReduction(TestCase testcase, UtilityData data) {
        BigInteger naiveVertexDomainSize = new BigInteger(String.valueOf(testcase.source.getGraph().vertexSet().size())).pow(testcase.target.getGraph().vertexSet().size());
        BigInteger vertexDomainSize = Arrays.stream(data.getCompatibility()).reduce(new BigInteger("1"), (i, vs) -> i.multiply(new BigInteger(String.valueOf(vs.length))), BigInteger::multiply);
        LOG.info(() -> "Reduced domain by a factor of " + (naiveVertexDomainSize.doubleValue() / vertexDomainSize.doubleValue()) + " to " + vertexDomainSize);
    }


    private static boolean allDone(Graph<Vertex, DefaultEdge> pattern, VertexMatching vertexMatching, EdgeMatching edgeMatching) {
        boolean completeV = vertexMatching.getPlacementUnsafe().size() == pattern.vertexSet().size();
        if (!completeV) {
            return false;
        }
        boolean completeE = !edgeMatching.hasUnmatched();
        if (!completeE) {
            return false;
        }
        LOG.info(() -> "Done, checking...");
        Util.isCorrect(pattern, vertexMatching, edgeMatching);
        return true;
    }


}
