package system;

import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.graph.generation.random.TrulyRandomDirectedTestCaseGenerator;
import com.charrey.graph.generation.random.TrulyRandomUndirectedTestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedUndirectedTestCaseGenerator;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.TimeoutResult;
import com.charrey.settings.pathiteration.PathIteration;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

class RandomSystemTests extends SystemTest {

    private final Settings settings = new SettingsBuilder()
            .withAllDifferentPruning()
            .withInplaceNewGreedyDFSRouting()
            .withCachedPruning().get();


    @Test
    void findCasesUndirectedRandom() throws IOException {
        if (settings.getPathIteration().iterationStrategy == PathIteration.KPATH) {
            return;
        }
        findCases(10 * 1000, 5, new TrulyRandomUndirectedTestCaseGenerator(1, 0, 1.5, 6), false);
    }

    @Test
    void findCasesUndirectedSucceed() throws IOException {
        if (settings.getPathIteration().iterationStrategy == PathIteration.KPATH) {
            return;
        }
        findCases(10 * 1000, 5, new RandomSucceedUndirectedTestCaseGenerator(1, 0, 0.1, 2, 30), true);
    }

    @Test
    void findCasesDirectedSucceed() throws IOException {
        findCases(100 * 1000, 50, new RandomSucceedDirectedTestCaseGenerator(1, 0, 0.1, 2, 30), true);
    }

    @Test
    void findCasesDirectedRandom() throws IOException {
        findCases(10 * 1000, 5, new TrulyRandomDirectedTestCaseGenerator(1, 0, 1.5, 6), false);
    }


    @SuppressWarnings("SameParameterValue")
    private void findCases(long time, int iterations, @NotNull TestCaseGenerator graphGen, boolean expectSucceed) throws IOException {
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
                //System.out.println("case " + attempts);
                if (attempts >= 0) {
                    try {
                        if (expectSucceed) {
                            homeomorphism = testSucceed(testCase, time - (System.currentTimeMillis() - start), settings);
                        } else {
                            homeomorphism = testWithoutExpectation(testCase, time - (System.currentTimeMillis() - start), settings);
                        }
                    } catch (AssertionError | IllegalStateException e) {
                        System.err.println(attempts);
                        System.err.println(testCase.getSourceGraph());
                        System.err.println(testCase.getTargetGraph());
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
