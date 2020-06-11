package unit.iterator;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.graph.generation.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.DFSPathIterator;
import com.charrey.pathiterators.yen.YenPathIterator;
import com.charrey.settings.PathIterationStrategy;
import com.charrey.settings.RunTimeCheck;
import com.charrey.settings.Settings;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.checker.DomainCheckerException;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NoDuplicatesTest {

    private final Random random = new Random(19247);

    @Test
    public void testArbitraryDFS() throws DomainCheckerException {
        testIterator(PathIterationStrategy.DFS_ARBITRARY);
    }

    @Test
    public void testGreedyDFS() throws DomainCheckerException {
        testIterator(PathIterationStrategy.DFS_GREEDY);
    }

    @Test

    public void testYen() throws DomainCheckerException {
        testIterator(PathIterationStrategy.YEN);
    }

    @Test
    public void testControlPoint() throws DomainCheckerException {
        testIterator(PathIterationStrategy.CONTROL_POINT);
    }

    private static final int differentGraphSizes = 250;
    private static final int trials = 10;

    public void testIterator(int strategy) throws DomainCheckerException {
        Settings.instance.pathIteration = strategy;
        Settings.instance.runTimeCheck = RunTimeCheck.NONE;
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
                Vertex tail = selectRandom(targetGraph.vertexSet(), x -> true);
                Vertex head = selectRandom(targetGraph.vertexSet(), x -> x != tail);
                counter++;
                System.out.println(counter + "/" + (differentGraphSizes * trials));
                if (Graphs.neighborSetOf(targetGraph, tail).contains(head)) {
                    continue;
                }
                Occupation occupation = new Occupation(data, targetGraph.vertexSet().size());
                occupation.occupyVertex(0, tail);
                occupation.occupyVertex(1, head);
                PathIterator iterator = PathIterator.get(targetGraph, data, tail, head, occupation, () -> 2);
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

    private <V> V selectRandom(Set<V> set, Predicate<V> eligable) {
        List<V> myList = set.stream().filter(eligable).collect(Collectors.toList());
        return myList.get(random.nextInt(myList.size()));
    }

    private static class Witness {

        private final String string;

        public Witness(PathIterator iterator) {
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
