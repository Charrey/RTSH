package system;

import com.charrey.Homeomorphism;
import com.charrey.graph.generation.RandomTestCaseGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomSystemTests extends SystemTest {

    private static final int ITERATIONS = 10;
    private static final long TIMEOUT = 60_000;


    @Test
    public void findCases() throws IOException {
        Logger.getLogger("IsoFinder").setLevel(Level.OFF);
        int patternNodes = 1;
        int patternEdges = 0;
        long start = System.currentTimeMillis();
        while (true) {
            RandomTestCaseGenerator graphGen = new RandomTestCaseGenerator(patternNodes, patternEdges, 0.1, 2);
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
            System.out.println(patternNodes + "\t" + patternEdges + "\t" + (total/ITERATIONS));
            if (patternEdges < (patternNodes * (patternNodes - 1))/2) {
                patternEdges++;
            } else {
                patternEdges = 0;
                patternNodes++;
            }

        }
    }
}
