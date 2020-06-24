package system;

import com.charrey.HomeomorphismResult;
import com.charrey.IsoFinder;
import com.charrey.graph.generation.*;
import com.charrey.graph.generation.random.TrulyRandomDirectedTestCaseGenerator;
import com.charrey.graph.generation.random.TrulyRandomUndirectedTestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedUndirectedTestCaseGenerator;
import com.charrey.settings.PathIterationStrategy;
import com.charrey.settings.RunTimeCheck;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

class RandomSystemTests extends SystemTest {

    private final Settings settings = new Settings(true, true, true, RunTimeCheck.ALL_DIFFERENT, PathIterationStrategy.CONTROL_POINT, new Random(1234));


    @Test
    void findCasesUndirectedRandom() throws IOException {
        if (settings.pathIteration == PathIterationStrategy.YEN) {
            return;
        }
        findCases(30*1000, 5, new TrulyRandomUndirectedTestCaseGenerator(1, 0, 1.5, 6), false);
    }

    @Test
    void findCasesUndirectedSucceed() throws IOException {
        if (settings.pathIteration == PathIterationStrategy.YEN) {
            return;
        }
        findCases(30*1000, 5, new RandomSucceedUndirectedTestCaseGenerator(1, 0, 0.1, 2, 30), true);
    }

    @Test
    void findCasesDirectedSucceed() throws IOException {
        findCases(30*1000, 5, new RandomSucceedDirectedTestCaseGenerator(1, 0, 0.1, 2, 30), true);
    }

    @Test
    void findCasesDirectedRandom() throws IOException {
        findCases(30*1000, 5, new TrulyRandomDirectedTestCaseGenerator(1, 0, 1.5, 6), false);
    }


    @SuppressWarnings("SameParameterValue")
    private void findCases(long time, int iterations, @NotNull TestCaseGenerator graphGen, boolean writeChallenge) throws IOException {
        Logger.getLogger("IsoFinder").setLevel(Level.OFF);
        long start = System.currentTimeMillis();
        double totalIterations = 0L;
        while (true) {
            graphGen.init(iterations, false);
            double total = 0.;
            int patternNodes = 0;
            int patternEdges = 0;

            int casesSucceed = 0;
            int casesFailed = 0;
            int casesCompatibilityFailed = 0;

            for (int i = 0; i < iterations; i++) {
                TestCase testCase = graphGen.getNext();
                System.out.println(testCase.sourceGraph.toString().replaceAll("\r\n", ""));
                System.out.println(testCase.targetGraph.toString().replaceAll("\r\n", ""));
                patternNodes = testCase.sourceGraph.vertexSet().size();
                patternEdges = testCase.sourceGraph.edgeSet().size();
                HomeomorphismResult homeomorphism = writeChallenge ? testSucceed(testCase, writeChallenge, time - (System.currentTimeMillis() - start), settings) : new IsoFinder().getHomeomorphism(testCase, settings, time - (System.currentTimeMillis() - start));
                if (homeomorphism == null) {
                    return;
                }
                total += homeomorphism.iterations;
                if (!homeomorphism.failed) {
                    casesSucceed++;
                } else if (homeomorphism.iterations == 0) {
                    casesCompatibilityFailed++;
                } else {
                    casesFailed++;
                }
            }
            totalIterations += (total/ (double) iterations);
            System.out.println(patternNodes + "\t" + patternEdges + "\t" + (long)totalIterations + "\t" + casesSucceed + "/" + iterations + "\t"+ casesCompatibilityFailed + "/" + iterations + "\t"+ casesFailed + "/" + iterations + "\t");
            graphGen.makeHarder();
        }
    }


}
