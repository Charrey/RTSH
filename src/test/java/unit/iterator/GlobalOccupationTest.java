package unit.iterator;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.PartialMatching;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.settings.iterator.*;
import com.charrey.util.Util;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalOccupationTest {

    private final RandomGenerator random = new Well512a(19247);
    private final static int differentGraphSizes = 250;
    private final static int trials = 20;

    private Settings settings = new SettingsBuilder()
            .withAllDifferentPruning()
            .withControlPointRouting(3)
            .withCachedPruning().get();

    @Test
    void testArbitraryDFS() throws DomainCheckerException {
        testIterator(new DFSStrategy());
    }

    @Test
    void testGreedyDFS() throws DomainCheckerException {
        testIterator(new OldGreedyDFSStrategy());
    }

    @Test
    void testYen() throws DomainCheckerException {
        testIterator(new KPathStrategy());
    }

    @Test
    void testControlPoint() throws DomainCheckerException {
        testIterator(new ControlPointIteratorStrategy(3));
    }

    void testIterator(IteratorSettings strategy) throws DomainCheckerException {
        settings = new SettingsBuilder(settings).withPathIteration(strategy).withoutPruning().get();
        final long seed = 1923;
        long counter = -1;
        RandomSucceedDirectedTestCaseGenerator gen = new RandomSucceedDirectedTestCaseGenerator(2, 1, 0, 0, seed);
        for (int i = 0; i < differentGraphSizes; i++) {
            gen.makeHarder();
            gen.init(trials);
            while (gen.hasNext()) {
                MyGraph targetGraph = gen.getNext().getSourceGraph();
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
                occupation.occupyVertex(0, tail, new PartialMatching(new TIntArrayList(), new TIntObjectHashMap<>(), new TIntHashSet()));
                TIntList vertexMatching = new TIntArrayList();
                vertexMatching.add(tail);
                occupation.occupyVertex(1, head, new PartialMatching(vertexMatching, new TIntObjectHashMap<>(), new TIntHashSet()));
                PathIterator iterator = PathIterator.get(targetGraph, data, tail, head, occupation, () -> 2, settings, () -> {
                    TIntList vertexMatching1 = new TIntArrayList();
                    vertexMatching1.add(tail);
                    vertexMatching1.add(head);
                    return new PartialMatching(vertexMatching1);
                }, Long.MAX_VALUE);
                Path path;
                while ((path = iterator.next()) != null) {
                    TIntSet occupationSays = occupation.getRoutingOccupied();
                    TIntSet pathSays = new TIntHashSet(path.intermediate().asList());
                    assertEquals(occupationSays, pathSays, "Iteration: " + counter);
                }
            }
        }
    }

}
