package unit.iterator;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pathiterators.PathIteratorFactory;
import com.charrey.pathiterators.kpath.KPathPathIterator;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.serial.PartialMatching;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.util.Util;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreedyDfsTest {

    private static final int differentGraphSizes = 1;
    private static final int trials = 10;
    private final RandomGenerator random = new Well512a(188);

    @Test
    void testIterator() throws DomainCheckerException {
        Settings settingsGreedy = new SettingsBuilder().withInplaceNewGreedyDFSRouting().get();
        Settings settingsKpath = new SettingsBuilder().withKPathRouting().get();

        final long seed = 1923;
        long counter = -1;
        RandomSucceedDirectedTestCaseGenerator gen = new RandomSucceedDirectedTestCaseGenerator(200, 1000, 0, 0, seed);
        for (int i = 0; i < differentGraphSizes; i++) {
            gen.makeHarder();
            gen.init(trials);
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
                System.out.print(counter + "/" + differentGraphSizes * trials + "\n");
                if (Graphs.neighborSetOf(targetGraph, tail).contains(head)) {
                    continue;
                }
                List<Integer> vertexMatching = new ArrayList<>();
                vertexMatching.add(tail);
                GlobalOccupation occupationGreedy = new GlobalOccupation(data, settingsGreedy);
                occupationGreedy.occupyVertex(0, tail, new PartialMatching());
                occupationGreedy.occupyVertex(1, head, new PartialMatching(vertexMatching));
                GlobalOccupation occupationKPath = new GlobalOccupation(data, settingsKpath);
                occupationKPath.occupyVertex(0, tail, new PartialMatching());
                occupationKPath.occupyVertex(1, head, new PartialMatching(vertexMatching));
                PathIterator greedyDFSIterator = PathIteratorFactory.get(targetGraph, data, tail, head, occupationGreedy, () -> 2, settingsGreedy, () -> {
                    List<Integer> vertexMatching12 = new ArrayList<>();
                    vertexMatching12.add(tail);
                    vertexMatching12.add(head);
                    return new PartialMatching(vertexMatching12);
                }, Long.MAX_VALUE, 0, -1, -1);
                KPathPathIterator kPathIterator = (KPathPathIterator) PathIteratorFactory.get(targetGraph, data, tail, head, occupationKPath, () -> 2, settingsKpath, () -> {
                    List<Integer> vertexMatching1 = new ArrayList<>();
                    vertexMatching1.add(tail);
                    vertexMatching1.add(head);
                    return new PartialMatching(vertexMatching1);
                }, Long.MAX_VALUE, 0, -1, -1);

                Path path1 = kPathIterator.next();
                Path path2 = greedyDFSIterator.next();
                try {
                    assertEquals(Objects.toString(path1), Objects.toString(path2));
                } catch (AssertionFailedError e) {
                    System.err.println(counter);
                    if (path1 == null) {
                        System.err.println("KPath could not be found!");
                    } else {
                        System.err.println("Cost of kpath: " + path1.getWeight());
                    }
                    if (path2 == null) {
                        System.err.println("DFS could not be found!");
                    } else {
                        System.err.println("Cost of dfs: " + path2.getWeight());
                    }
                    throw e;
                }
            }
        }
    }
}
