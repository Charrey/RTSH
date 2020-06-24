package unit.iterator;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.PathIterationConstants;
import com.charrey.settings.PruningConstants;
import com.charrey.settings.Settings;
import com.charrey.util.Util;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalOccupationTest {

    private final Random random = new Random(19247);
    private final static int differentGraphSizes = 250;
    private final static int trials = 20;

    private final Settings settings = new Settings(
            true,
            true,
            true,
            PruningConstants.ALL_DIFFERENT,
            PathIterationConstants.CONTROL_POINT
    );

    @Test
    void testArbitraryDFS() throws DomainCheckerException {
        testIterator(PathIterationConstants.DFS_ARBITRARY);
    }

    @Test
    void testGreedyDFS() throws DomainCheckerException {
        testIterator(PathIterationConstants.DFS_GREEDY);
    }

    @Test
    void testYen() throws DomainCheckerException {
        testIterator(PathIterationConstants.KPATH);
    }

    @Test
    void testControlPoint() throws DomainCheckerException {
        testIterator(PathIterationConstants.CONTROL_POINT);
    }

    void testIterator(int strategy) throws DomainCheckerException {
        settings.pathIteration = strategy;
        settings.pruningMethod = PruningConstants.NONE;
        final long seed = 1923;
        long counter = -1;
        RandomSucceedDirectedTestCaseGenerator gen = new RandomSucceedDirectedTestCaseGenerator(2, 1, 0, 0, seed);
        for (int i = 0; i < differentGraphSizes; i++) {
            gen.makeHarder();
            gen.init(trials, false);
            while (gen.hasNext()) {
                MyGraph targetGraph = gen.getNext().sourceGraph;
                MyGraph sourceGraph = new MyGraph(true);
                sourceGraph.addEdge(sourceGraph.addVertex(), sourceGraph.addVertex());
                UtilityData data = new UtilityData(sourceGraph, targetGraph);
                int tail = Util.selectRandom(targetGraph.vertexSet(), x -> true, random);
                int head = Util.selectRandom(targetGraph.vertexSet(), x -> x != tail, random);
                counter++;
                if (counter < 173) {
                    continue;
                }
                System.out.print(counter % 100 == 0 ? counter + "/" + differentGraphSizes * trials + "\n" : "");
                if (Graphs.neighborSetOf(targetGraph, tail).contains(head)) {
                    continue;
                }
                GlobalOccupation occupation = new GlobalOccupation(data, settings);
                occupation.occupyVertex(0, tail);
                occupation.occupyVertex(1, head);
                PathIterator iterator = PathIterator.get(targetGraph, data, tail, head, occupation, () -> 2, settings);
                Path path;
                while ((path = iterator.next()) != null) {
                    Set<Integer> occupationSays = occupation.getRoutingOccupied();
                    Set<Integer> pathSays = new HashSet<>(path.intermediate());
                    assertEquals(occupationSays, pathSays, "Iteration: " + counter);
                }
            }
        }
    }

}
