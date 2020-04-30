package system;

import com.charrey.Homeomorphism;
import com.charrey.graph.generation.RandomTestCaseGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomSystemTests extends SystemTest {

    private static final int ITERATIONS = 200;
    private static final long TIMEOUT = Long.MAX_VALUE;


    @Test
    public void findCases() throws IOException {
        Logger.getLogger("IsoFinder").setLevel(Level.OFF);
        int patternNodes = 1;
        int patternEdges = 0;
        long start = System.currentTimeMillis();
        double totalIterations = 0L;
        Random random = new Random(670);
        while (patternNodes < 8) {
            RandomTestCaseGenerator graphGen = new RandomTestCaseGenerator(patternNodes, patternEdges, 0.1, 2, random.nextInt());
            graphGen.init(ITERATIONS, false);
            double total = 0.;
            for (int i = 0; i < ITERATIONS; i++) {
                if (System.currentTimeMillis() - start > TIMEOUT) {
                    return;
                }
                RandomTestCaseGenerator.TestCase testCase = graphGen.getNext();
                Homeomorphism homeomorphism = testSucceed(testCase, true);
                total += homeomorphism.getIterations();
            }
            totalIterations += (total/ (double) ITERATIONS);
            System.out.println(patternNodes + "\t" + patternEdges + "\t" + totalIterations);
            if (patternEdges < (patternNodes * (patternNodes - 1))/2) {
                patternEdges++;
            } else {
                patternEdges = 0;
                patternNodes++;
            }

        }
    }
}
