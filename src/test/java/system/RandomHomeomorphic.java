package system;

import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.generation.TestCaseGenerator;
import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomHomeomorphic extends SystemTest {


    private static final int ITERATIONS = 500;

    @Test
    public void systemTestSucceed() throws IOException, ClassNotFoundException {
        Logger.getLogger("IsoFinder").setLevel(Level.OFF);
        showProgress();
        Pair<GraphGeneration, GraphGeneration> challenge = readChallenge();
        testSucceed(challenge, true);
        for (int i = 0; i < ITERATIONS; i++) {
            Pair<GraphGeneration, GraphGeneration> pair = TestCaseGenerator.getRandom(10, 15, 1, 2);
            testSucceed(pair, false);
            printPercentage(i, ITERATIONS);
        }
    }


}
