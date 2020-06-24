package com.charrey;

import com.charrey.algorithms.GreatestConstrainedFirst;
import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.TestCase;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Logger;


public class IsoFinder {

    private static final Logger LOG = Logger.getLogger("IsoFinder");
    private EdgeMatching edgeMatching;
    private VertexMatching vertexMatching;

    private void setup(@NotNull TestCase testcase, @NotNull Settings settings) throws DomainCheckerException {
        UtilityData data = new UtilityData(testcase.sourceGraph, testcase.targetGraph);
        logDomainReduction(testcase, data, settings.initialNeighbourhoodFiltering, settings.initialGlobalAllDifferent);

        if (Arrays.stream(data.getCompatibility(settings.initialNeighbourhoodFiltering, settings.initialGlobalAllDifferent)).anyMatch(x -> x.length == 0)) {
            throw new DomainCheckerException("Intial domain check failed");
        }
        GlobalOccupation occupation = new GlobalOccupation(data, settings.runTimeCheck, settings.initialNeighbourhoodFiltering, settings.initialGlobalAllDifferent);
        vertexMatching      = new VertexMatching(data, testcase.sourceGraph, occupation, settings.initialNeighbourhoodFiltering, settings.initialGlobalAllDifferent);
        edgeMatching        = new EdgeMatching(vertexMatching, data, testcase.sourceGraph, testcase.targetGraph, occupation, settings.pathIteration, settings.refuseLongerPaths);
    }

    private long lastPrint = 0;
    @Nullable
    public HomeomorphismResult getHomeomorphism(@NotNull TestCase testcase, @NotNull Settings settings, long timeout) {
        try {
            testcase.sourceGraph = new GreatestConstrainedFirst().apply(testcase.sourceGraph);
            setup(testcase, settings);
        } catch (DomainCheckerException e) {
            return HomeomorphismResult.COMPATIBILITY_FAIL;
        }
        long iterations = 0;
        long initialTime = System.currentTimeMillis();
        while (!allDone(testcase.sourceGraph, vertexMatching, edgeMatching)) {
            iterations++;
            if (System.currentTimeMillis() - lastPrint > 2000) {
                System.out.println(iterations + " iterations...");
                lastPrint = System.currentTimeMillis();
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



    private static void logDomainReduction(@NotNull TestCase testcase, @NotNull UtilityData data, boolean initialNeighbourHoodFiltering, boolean initialGlobalAllDifferent) {
        BigInteger naiveVertexDomainSize = new BigInteger(String.valueOf(testcase.sourceGraph.vertexSet().size())).pow(testcase.targetGraph.vertexSet().size());
        BigInteger vertexDomainSize = Arrays.stream(data.getCompatibility(initialNeighbourHoodFiltering, initialGlobalAllDifferent)).reduce(new BigInteger("1"), (i, vs) -> i.multiply(new BigInteger(String.valueOf(vs.length))), BigInteger::multiply);
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
