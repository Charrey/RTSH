package com.charrey;

import com.charrey.graph.Path;
import com.charrey.graph.generation.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.settings.Settings;
import com.charrey.algorithms.UtilityData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.charrey.runtimecheck.DomainCheckerException;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Logger;


public class IsoFinder {

    private static final Logger LOG = Logger.getLogger("IsoFinder");
    private static EdgeMatching edgeMatching;
    private static VertexMatching vertexMatching;

    private static void setup(@NotNull TestCase testcase, @NotNull Settings settings) throws DomainCheckerException {
        UtilityData data = new UtilityData(testcase.sourceGraph, testcase.targetGraph);
        logDomainReduction(testcase, data, settings.initialLocalizedAllDifferent, settings.initialGlobalAllDifferent);
        if (Arrays.stream(data.getCompatibility(settings.initialLocalizedAllDifferent, settings.initialGlobalAllDifferent)).anyMatch(x -> x.length == 0)) {
            throw new DomainCheckerException("Intial domain check failed");
        }
        Occupation occupation = new Occupation(data, testcase.targetGraph.vertexSet().size(), settings.runTimeCheck, settings.initialLocalizedAllDifferent, settings.initialGlobalAllDifferent);
        vertexMatching      = new VertexMatching(data, testcase.sourceGraph, occupation, settings.initialLocalizedAllDifferent, settings.initialGlobalAllDifferent);
        edgeMatching        = new EdgeMatching(vertexMatching, data, testcase.sourceGraph, testcase.targetGraph, occupation, settings.pathIteration, settings.refuseLongerPaths);
    }

    @Nullable
    public static HomeomorphismResult getHomeomorphism(@NotNull TestCase testcase, @NotNull Settings settings, long timeout) {
        try {
            setup(testcase, settings);
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



    private static void logDomainReduction(@NotNull TestCase testcase, @NotNull UtilityData data, boolean initialLocalizedAllDifferent, boolean initialGlobalAllDifferent) {
        BigInteger naiveVertexDomainSize = new BigInteger(String.valueOf(testcase.sourceGraph.vertexSet().size())).pow(testcase.targetGraph.vertexSet().size());
        BigInteger vertexDomainSize = Arrays.stream(data.getCompatibility(initialLocalizedAllDifferent, initialGlobalAllDifferent)).reduce(new BigInteger("1"), (i, vs) -> i.multiply(new BigInteger(String.valueOf(vs.length))), BigInteger::multiply);
        LOG.info(() -> "Reduced vertex matching domain by a factor of " + (naiveVertexDomainSize.doubleValue() / vertexDomainSize.doubleValue()) + " to " + vertexDomainSize);
    }


    private static boolean allDone(@NotNull MyGraph pattern, @NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
        boolean completeV = vertexMatching.getPlacementUnsafe().size() == pattern.vertexSet().size();
        if (!completeV) {
            return false;
        }
        boolean completeE = !edgeMatching.hasUnmatched();
        if (!completeE) {
            return false;
        }
        LOG.info(() -> "Done, checking...");
        boolean correct = Verifier.isCorrect(pattern, vertexMatching, edgeMatching);
        assert correct;
        return true;
    }


}
