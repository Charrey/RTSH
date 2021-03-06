package system;

import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.graph.generation.random.TrulyRandomDirectedTestCaseGenerator;
import com.charrey.graph.generation.random.TrulyRandomUndirectedTestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedUndirectedTestCaseGenerator;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.TimeoutResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.settings.pathiteration.PathIteration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.fail;

public class VisitStateOnce extends SystemTest {

    private final Settings settings = new SettingsBuilder()
            .withAllDifferentPruning()
            .withInplaceNewGreedyDFSRouting()
            .withoutContraction()
            .withCachedPruning().get();


    @Test
    void findCasesUndirectedRandom() throws IOException {
        if (settings.getPathIteration().iterationStrategy == PathIteration.KPATH) {
            return;
        }
        findCases(100 * 1000, 50, new TrulyRandomUndirectedTestCaseGenerator(1, 0, 1.5, 6), false);
    }

    @Test
    void findCasesUndirectedSucceed() throws IOException {
        if (settings.getPathIteration().iterationStrategy == PathIteration.KPATH) {
            return;
        }
        findCases(100 * 1000, 50, new RandomSucceedUndirectedTestCaseGenerator(1, 0, 0.1, 2, 30), true);
    }

    @Test
    void findCasesDirectedSucceed() throws IOException {
        findCases(100 * 1000, 50, new RandomSucceedDirectedTestCaseGenerator(1, 0, 0.1, 2, 30), true);
    }

    @Test
    void findCasesDirectedRandom() throws IOException {
        findCases(100 * 1000, 50, new TrulyRandomDirectedTestCaseGenerator(1, 0, 1.5, 6), false);
    }

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
                Arrays.stream(Logger.getLogger("IsoFinder").getHandlers()).forEach(x -> Logger.getLogger("IsoFinder").removeHandler(x));
                Logger.getLogger("IsoFinder").setLevel(Level.ALL);
                Logger.getLogger("IsoFinder").addHandler(new TestHandler(attempts));
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

    private static class TestHandler extends Handler {

        private final LinkedHashSet<String> set = new LinkedHashSet<>();
        private final long caseNo;
        private String last = null;

        private TestHandler(long caseNo){
            this.caseNo = caseNo;
        }

        @Override
        public void publish(LogRecord logRecord) {
            if (!logRecord.getMessage().equals(last)) {
                last = logRecord.getMessage();
                if (set.contains(last)) {
                    System.err.println(set);
                    System.err.println("Case: " + caseNo);
                    System.err.println(last);
                    fail();
                }
                set.add(last);
            }
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }
}
