package unit.iterator;

import com.charrey.occupation.GlobalOccupation;
import com.charrey.algorithms.UtilityData;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.DFSPathIterator;
import com.charrey.pathiterators.yen.YenPathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.PathIterationStrategy;
import com.charrey.settings.RunTimeCheck;
import com.charrey.settings.Settings;
import com.charrey.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class NoDuplicatesTest {

    private final Random random = new Random(19247);

    private final Settings settings = new Settings(
            true,
            true,
            true,
            RunTimeCheck.ALL_DIFFERENT,
            PathIterationStrategy.CONTROL_POINT,
            random
    );

    @Test
    void testArbitraryDFS() throws DomainCheckerException {
        testIterator(PathIterationStrategy.DFS_ARBITRARY);
    }

    @Test
    void testGreedyDFS() throws DomainCheckerException {
        testIterator(PathIterationStrategy.DFS_GREEDY);
    }

    @Test
    void testYen() throws DomainCheckerException {
        testIterator(PathIterationStrategy.YEN);
    }

    @Test
    void testControlPoint() throws DomainCheckerException {
        testIterator(PathIterationStrategy.CONTROL_POINT);
    }

    private static final int differentGraphSizes = 250;
    private static final int trials = 10;

    void testIterator(int strategy) throws DomainCheckerException {
        settings.pathIteration = strategy;
        settings.runTimeCheck = RunTimeCheck.NONE;
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
                Vertex tail = Util.selectRandom(targetGraph.vertexSet(), x -> true, random);
                Vertex head = Util.selectRandom(targetGraph.vertexSet(), x -> x != tail, random);
                counter++;
                System.out.print(counter % 100 == 0 ? counter + "/" + differentGraphSizes * trials + "\n" : "");
                if (Graphs.neighborSetOf(targetGraph, tail).contains(head)) {
                    continue;
                }
                GlobalOccupation occupation = new GlobalOccupation(data, targetGraph.vertexSet().size(), settings);
                occupation.occupyVertex(0, tail);
                occupation.occupyVertex(1, head);
                PathIterator iterator = PathIterator.get(targetGraph, data, tail, head, occupation, () -> 2, settings);
                Map<Path, Witness> seen = new HashMap<>();
                Path path;
                //5 4 1 3 2 is reached with controlpoints {4, 3} and with {4, 1, 3}.
                //emulate fails with controlpoints 4, 3 and no local occupation.
                while ((path = iterator.next()) != null) {
                    assert !seen.containsKey(path) : "\nPath:     " + path + "\nPrevious: " + seen.get(path).string + "\nNow:      " + new Witness(iterator).string + "\nAt:\n" + targetGraph.toString();
                    seen.put(new Path(path), new Witness(iterator));
                }
            }
        }
    }



    private static class Witness {

        @NotNull
        private final String string;

        Witness(PathIterator iterator) {
            if (iterator instanceof ManagedControlPointIterator) {
                string = "Control points: " + ((ManagedControlPointIterator) iterator).controlPoints();
            } else if (iterator instanceof DFSPathIterator) {
                string = "DFS path iterator";
            } else if (iterator instanceof YenPathIterator) {
                string = "YEN path iterator";
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
