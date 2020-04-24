package system;

import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.generation.RandomTestCaseGenerator;
import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomHomeomorphic extends SystemTest {


    private static final int ITERATIONS = 200;

    @Test
    public void systemTestSucceed() throws IOException, ClassNotFoundException {
        Logger.getLogger("IsoFinder").setLevel(Level.OFF);
        Pair<GraphGeneration, GraphGeneration> challenge = readChallenge();
        if (challenge != null) {
            testSucceed(new RandomTestCaseGenerator.TestCase(challenge.getFirst(), challenge.getSecond()));
        }
        RandomTestCaseGenerator graphGen = new RandomTestCaseGenerator(6, 8, 3, 2);
        graphGen.init(ITERATIONS);
        //showProgress();
        for (int i = 0; i < ITERATIONS; i++) {
            RandomTestCaseGenerator.TestCase testCase = graphGen.getNext();
            testSucceed(testCase);
            printPercentage(i, ITERATIONS);
        }
    }


}
