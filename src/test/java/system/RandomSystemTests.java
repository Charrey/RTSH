package system;

import com.charrey.HomeomorphismResult;
import com.charrey.IsoFinder;
import com.charrey.graph.generation.RandomSucceedTestCaseGenerator;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.graph.generation.TrulyRandomTestCaseGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomSystemTests extends SystemTest {

    @Test
    public void findCasesRandom() throws IOException {
        findCases(60*1000, 100, new TrulyRandomTestCaseGenerator(1, 0, 1.5, 6), false);
    }

    @Test
    public void findCasesSucceed() throws IOException {
        findCases(6000*1000, 1, new RandomSucceedTestCaseGenerator(1, 0, 0.1, 2, 30), true);
    }


    private void findCases(long time, int iterations, TestCaseGenerator graphGen, boolean writeChallenge) throws IOException {
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
                if (System.currentTimeMillis() - start > time) {
                    return;
                }
                TestCase testCase = graphGen.getNext();
                patternNodes = testCase.source.getGraph().vertexSet().size();
                patternEdges = testCase.source.getGraph().edgeSet().size();
                HomeomorphismResult homeomorphism = writeChallenge ? testSucceed(testCase, writeChallenge) : IsoFinder.getHomeomorphism(testCase);
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
