package com.charrey;

import com.charrey.graph.Path;
import com.charrey.graph.generation.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.util.Util;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.checker.DomainCheckerException;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Logger;


public class IsoFinder {

    private static final Logger LOG = Logger.getLogger("IsoFinder");
    private static EdgeMatching edgeMatching;
    private static VertexMatching vertexMatching;

    private static void setup(TestCase testcase) throws DomainCheckerException {
        UtilityData data = new UtilityData(testcase.sourceGraph, testcase.targetGraph);
        logDomainReduction(testcase, data);
        if (Arrays.stream(data.getCompatibility()).anyMatch(x -> x.length == 0)) {
            throw new DomainCheckerException("Intial domain check failed");
        }
        Occupation occupation = new Occupation(data, testcase.targetGraph.vertexSet().size());
        vertexMatching      = new VertexMatching(data, testcase.sourceGraph, occupation);
        edgeMatching        = new EdgeMatching(vertexMatching, data, testcase.sourceGraph, testcase.targetGraph, occupation);
    }

    public static HomeomorphismResult getHomeomorphism(TestCase testcase, long timeout) {
        try {
            setup(testcase);
        } catch (DomainCheckerException e) {
            return HomeomorphismResult.COMPATIBILITY_FAIL;
        }
        long iterations = 0;
        long initialTime = System.currentTimeMillis();
        while (!allDone(testcase.sourceGraph, vertexMatching, edgeMatching)) {
            iterations++;
            if (iterations%1000==0) {
                System.out.println(iterations + " iterations...");
            }
            if (System.currentTimeMillis() > initialTime + timeout) {
                return null;
            }
            LOG.fine(() -> vertexMatching.toString() + "\n" + edgeMatching.toString());
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
        if (vertexMatching.getPlacementUnsafe().size() < testcase.sourceGraph.vertexSet().size()) {
            return HomeomorphismResult.ofFailed(iterations);
        } else {
            return HomeomorphismResult.ofSucceed(vertexMatching, edgeMatching, iterations);
        }
    }



    private static void logDomainReduction(TestCase testcase, UtilityData data) {
        BigInteger naiveVertexDomainSize = new BigInteger(String.valueOf(testcase.sourceGraph.vertexSet().size())).pow(testcase.targetGraph.vertexSet().size());
        BigInteger vertexDomainSize = Arrays.stream(data.getCompatibility()).reduce(new BigInteger("1"), (i, vs) -> i.multiply(new BigInteger(String.valueOf(vs.length))), BigInteger::multiply);
        LOG.info(() -> "Reduced vertex matching domain by a factor of " + (naiveVertexDomainSize.doubleValue() / vertexDomainSize.doubleValue()) + " to " + vertexDomainSize);
    }


    private static boolean allDone(MyGraph pattern, VertexMatching vertexMatching, EdgeMatching edgeMatching) {
        boolean completeV = vertexMatching.getPlacementUnsafe().size() == pattern.vertexSet().size();
        if (!completeV) {
            return false;
        }
        boolean completeE = !edgeMatching.hasUnmatched();
        if (!completeE) {
            return false;
        }
        LOG.info(() -> "Done, checking...");
        boolean correct = Util.isCorrect(pattern, vertexMatching, edgeMatching);
        assert correct;
        return true;
    }


}
