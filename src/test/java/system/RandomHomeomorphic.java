package system;
import com.charrey.Homeomorphism;
import com.charrey.IsoFinder;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.generation.GraphGenerator;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.util.GraphUtil;
import com.charrey.util.Util;
import com.charrey.util.UtilityData;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class RandomHomeomorphic extends SystemTest {

    @Test
    public void testChallenge() throws IOException, ClassNotFoundException {
        //Logger.getLogger("IsoFinder").setLevel(Level.ALL);
        Pair<GraphGeneration, GraphGeneration> challenge = readChallenge();
        testSucceed(challenge);
    }



    @Test
    public void testRandom() {
        while (true) {
            long seed = new Random().nextLong();
            Pair<GraphGeneration, GraphGeneration> pair = TestCaseGenerator.getRandom(4, 4, 1, 1, seed);
            testSucceed(pair);
        }
    }

    private void writeChallenge(Pair<GraphGeneration, GraphGeneration> pair) throws IOException {
        File file = new File("challenge.txt");
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(pair);
    }

    private Pair<GraphGeneration, GraphGeneration> readChallenge() throws IOException, ClassNotFoundException {
        try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(new File("challenge.txt")))) {
            return (Pair<GraphGeneration, GraphGeneration>) oos.readObject();
        }
    }



}
