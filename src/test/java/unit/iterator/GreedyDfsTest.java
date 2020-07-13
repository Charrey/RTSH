package unit.iterator;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pathiterators.dfs.DFSPathIterator;
import com.charrey.pathiterators.kpath.KPathPathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.Settings;
import com.charrey.settings.iterator.GreedyDFSStrategy;
import com.charrey.settings.iterator.KPathStrategy;
import com.charrey.settings.pruning.PruningApplicationConstants;
import com.charrey.settings.pruning.PruningConstants;
import com.charrey.settings.pruning.domainfilter.LabelDegreeFiltering;
import com.charrey.util.Util;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreedyDfsTest {

    private static final int differentGraphSizes = 1;
    private static final int trials = 10;
    private final RandomGenerator random = new Well512a(188);

    @Test
    void testIterator() throws DomainCheckerException {
        Settings settingsGreedy = new Settings(new LabelDegreeFiltering(), true, PruningConstants.NONE, new GreedyDFSStrategy(), PruningApplicationConstants.SERIAL);
        Settings settingsKpath = new Settings(new LabelDegreeFiltering(), true, PruningConstants.NONE, new KPathStrategy(), PruningApplicationConstants.SERIAL);

        final long seed = 1923;
        long counter = -1;
        RandomSucceedDirectedTestCaseGenerator gen = new RandomSucceedDirectedTestCaseGenerator(200, 1000, 0, 0, seed);
        for (int i = 0; i < differentGraphSizes; i++) {
            gen.makeHarder();
            gen.init(trials, false);
            while (gen.hasNext()) {
                MyGraph targetGraph = gen.getNext().getSourceGraph();
                MyGraph sourceGraph = new MyGraph(true);
                sourceGraph.addEdge(sourceGraph.addVertex(), sourceGraph.addVertex());
                sourceGraph.randomizeWeights();
                UtilityData data = new UtilityData(sourceGraph, targetGraph);
                int tail = Util.selectRandom(targetGraph.vertexSet(), x -> true, random);
                int head = Util.selectRandom(targetGraph.vertexSet(), x -> x != tail, random);
                counter++;
                if (counter < 0) {
                    continue;
                }
                System.out.print(true ? counter + "/" + differentGraphSizes * trials + "\n" : "");
                if (Graphs.neighborSetOf(targetGraph, tail).contains(head)) {
                    continue;
                }
                GlobalOccupation occupationGreedy = new GlobalOccupation(data, settingsGreedy, "Greedy");
                occupationGreedy.occupyVertex(0, tail);
                occupationGreedy.occupyVertex(1, head);
                GlobalOccupation occupationKPath = new GlobalOccupation(data, settingsKpath, "KPath");
                occupationKPath.occupyVertex(0, tail);
                occupationKPath.occupyVertex(1, head);
                DFSPathIterator greedyDFSIterator = (DFSPathIterator) PathIterator.get(targetGraph, data, tail, head, occupationGreedy, () -> 2, settingsGreedy);
                KPathPathIterator kPathIterator = (KPathPathIterator) PathIterator.get(targetGraph, data, tail, head, occupationKPath, () -> 2, settingsKpath);

                Path path1 = kPathIterator.next();
                Path path2 = greedyDFSIterator.next();
                try {
                    assertEquals(path1, path2);
                } catch (AssertionFailedError e) {
                    System.err.println(counter);
                    System.err.println("Cost of kpath: " + path1.getWeight());
                    System.err.println("Cost of dfs: " + path2.getWeight());
                    throw e;
                }
            }
        }
    }
}
