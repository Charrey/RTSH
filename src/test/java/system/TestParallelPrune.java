package system;

import com.charrey.IsoFinder;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.TimeoutResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class TestParallelPrune extends SystemTest {

    private final Settings settings = new SettingsBuilder()
            .withZeroDomainPruning()
            .withControlPointRouting(5)
            .withoutContraction()
            .withParallelPruning().get();

    @Test
    void findCasesDirectedSucceed() throws IOException {
        findCases(100 * 1000, 5, new RandomSucceedDirectedTestCaseGenerator(1, 0, 0.1, 2, 30), true);
    }

    private void findCases(long time, int iterations, @NotNull TestCaseGenerator graphGen, boolean writeChallenge) throws IOException {
        Logger.getLogger("IsoFinder").setLevel(Level.OFF);
        long start = System.currentTimeMillis();
        double totalIterations = 0L;
        long attempts = 0;
        while (true) {
            graphGen.init(iterations);
            double total = 0.;
            int patternNodes = 0;
            int patternEdges = 0;

            int casesSucceed = 0;
            int casesFailed = 0;
            int casesCompatibilityFailed = 0;


            for (int i = 0; i < iterations; i++) {
                TestCase testCase = graphGen.getNext();
                patternNodes = testCase.getSourceGraph().vertexSet().size();
                patternEdges = testCase.getSourceGraph().edgeSet().size();
                HomeomorphismResult homeomorphism;

                if (attempts >= 0) {
                    try {
                        homeomorphism = writeChallenge ? testSucceed(testCase, time - (System.currentTimeMillis() - start), settings) : new IsoFinder().getHomeomorphism(testCase, settings, time - (System.currentTimeMillis() - start), "RANDOMSYSTEST  ", false);
                        assert homeomorphism instanceof TimeoutResult || homeomorphism.succeed || !writeChallenge;
                    } catch (AssertionError e) {
                        System.err.println(attempts);
                        throw e;
                    }
                    if (homeomorphism instanceof TimeoutResult) {
                        return;
                    }
                    total += homeomorphism.iterations;
                    if (homeomorphism.succeed) {
                        casesSucceed++;
                    } else if (homeomorphism.iterations == 0) {
                        casesCompatibilityFailed++;
                    } else {
                        casesFailed++;
                    }
                }
                attempts++;
            }
            totalIterations += (total / iterations);
            System.out.println(patternNodes + "\t" + patternEdges + "\t" + (long) totalIterations + "\t" + casesSucceed + "/" + iterations + "\t" + casesCompatibilityFailed + "/" + iterations + "\t" + casesFailed + "/" + iterations + "\t");
            graphGen.makeHarder();
        }
    }


}
