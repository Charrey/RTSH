package system;
import com.charrey.Homeomorphism;
import com.charrey.IsoFinder;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.util.GraphUtil;
import com.charrey.util.Util;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RandomHomeomorphic {

    @Test
    public void testSucceed() {
        //new Thread(new Charter("benchmark.txt")).start();
        Graph<Vertex, DefaultEdge> previous = null;
        long seed = 1;
        for (int i = 0; i < 100; i++) {
            Pair<GraphGeneration, GraphGeneration> pair = TestCaseGenerator.getRandom(4, 0, 2, 3, seed);
            try {
                long start = System.currentTimeMillis();
                Optional<Homeomorphism> morph = IsoFinder.getHomeomorphism(pair.getFirst(), pair.getSecond());
                assert morph.isPresent();
                System.out.println(System.currentTimeMillis() - start);
            } catch (AssertionError e) {
                System.out.println(seed);
                System.out.println(pair.getFirst());
                System.out.println(pair.getSecond());
                fail();
            }
            seed += 1;
        }
    }
}
