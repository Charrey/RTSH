package unit.iterator;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.DFSPathIterator;
import com.charrey.pathiterators.kpath.KPathPathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.Settings;
import com.charrey.settings.iterator.*;
import com.charrey.settings.pruning.PruningApplicationConstants;
import com.charrey.settings.pruning.PruningConstants;
import com.charrey.settings.pruning.domainfilter.LabelDegreeFiltering;
import com.charrey.util.Util;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class NoDuplicatesTest {

    private final RandomGenerator random = new Well512a(19247);

    private final Settings settings = new Settings(
            new LabelDegreeFiltering(),
            true,
            PruningConstants.ALL_DIFFERENT,
            new ControlPointIteratorStrategy(5), PruningApplicationConstants.SERIAL
    );

    @Test
    void testArbitraryDFS() throws DomainCheckerException {
        testIterator(new DFSStrategy());
    }

    @Test
    void testGreedyDFS() throws DomainCheckerException {
        testIterator(new GreedyDFSStrategy());
    }

    @Test
    void testYen() throws DomainCheckerException {
        testIterator(new KPathStrategy());
    }

    @Test
    void testControlPoint() throws DomainCheckerException {
        testIterator(new ControlPointIteratorStrategy(3));
    }

    private static final int differentGraphSizes = 250;
    private static final int trials = 10;

    void testIterator(IteratorSettings strategy) throws DomainCheckerException {
        settings.pathIteration = strategy;
        settings.pruningMethod = PruningConstants.NONE;
        final long seed = 1923;
        long counter = -1;
        RandomSucceedDirectedTestCaseGenerator gen = new RandomSucceedDirectedTestCaseGenerator(2, 1, 0, 0, seed);
        for (int i = 0; i < differentGraphSizes; i++) {
            gen.makeHarder();
            gen.init(trials, false);
            while (gen.hasNext()) {
                MyGraph targetGraph = gen.getNext().getSourceGraph();
                MyGraph sourceGraph = new MyGraph(true);
                sourceGraph.addEdge(sourceGraph.addVertex(), sourceGraph.addVertex());
                UtilityData data = new UtilityData(sourceGraph, targetGraph);
                int tail = Util.selectRandom(targetGraph.vertexSet(), x -> true, random);
                int head = Util.selectRandom(targetGraph.vertexSet(), x -> x != tail, random);
                counter++;
                if (counter < 0) {
                    continue;
                }
                System.out.print(counter % 100 == 0 ? counter + "/" + differentGraphSizes * trials + "\n" : "");
                if (Graphs.neighborSetOf(targetGraph, tail).contains(head)) {
                    continue;
                }
                GlobalOccupation occupation = new GlobalOccupation(data, settings, "NoDuplicatesTest");
                occupation.occupyVertex(0, tail);
                occupation.occupyVertex(1, head);
                PathIterator iterator = PathIterator.get(targetGraph, data, tail, head, occupation, () -> 2, settings);
                Map<Path, Witness> seen = new HashMap<>();
                Path path;
                //5 4 1 3 2 is reached with controlpoints {4, 3} and with {4, 1, 3}.
                //emulate fails with controlpoints 4, 3 and no local occupation.
                while ((path = iterator.next()) != null) {
                    if (seen.containsKey(path)) {
                        System.out.println("Iteration: " + counter);
                        System.out.println("Path:      " + path);
                        System.out.println("Previous:  " + seen.get(path).string);
                        System.out.println("Now:       " + new Witness(iterator).string);
                        System.out.println("At:");
                        System.out.println(targetGraph.toString());
                        assert false;
                    }
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
            } else if (iterator instanceof KPathPathIterator) {
                string = "YEN path iterator";
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
