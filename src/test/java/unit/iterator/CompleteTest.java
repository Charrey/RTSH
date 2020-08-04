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
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

class CompleteTest extends PathIteratorTest {

    private final RandomGenerator random = new Well512a(102038);
    private static final int differentGraphSizes = 250;
    private static final int trials = 100;
    private static Settings settings = new SettingsBuilder()
            .withKPathRouting().get();

    @NotNull
    private static String myMaptoString(@NotNull Map<IteratorSettings, Set<Path>> pathCount) {
        StringBuilder sb = new StringBuilder();
        Set<Path> common = new HashSet<>();
        pathCount.values().forEach(common::addAll);
        new HashSet<>(common).forEach(path -> {
            if (pathCount.entrySet().stream().anyMatch(x -> !x.getValue().contains(path))) {
                common.remove(path);
            }
        });
        for (Map.Entry<IteratorSettings, Set<Path>> entry : pathCount.entrySet()) {
            List<Path> extra = entry.getValue().stream().filter(x -> !common.contains(x)).sorted().collect(Collectors.toList());
            sb.append("Option ").append(entry.getKey()).append(":\t").append(common.size()).append(" common and ").append(extra.isEmpty() ? "nothing else" : extra).append("\n");
        }
        return sb.toString();
    }

    @Test
    void testIterators() {
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
                if (Graphs.neighborSetOf(targetGraph, tail).contains(head)) {
                    continue;
                }
                counter++;
                if (counter < 0) {
                    continue;
                }
                System.out.print(counter % 100 == 0 ? counter + "/" + differentGraphSizes * trials + "\n" : "");
                Map<IteratorSettings, Set<Path>> pathCount = new HashMap<>(); //s
                for (IteratorSettings strategy : List.of(new DFSStrategy(), new OldGreedyDFSStrategy(), new ControlPointIteratorStrategy(100), new KPathStrategy())) {
                    settings = new SettingsBuilder(settings).withPathIteration(strategy).get();
                    pathCount.put(strategy, new HashSet<>());
                    GlobalOccupation occupation = new GlobalOccupation(data, settings);
                    TIntList vertexOccupation;
                    try {
                        occupation.occupyVertex(0, tail, new PartialMatching());
                        vertexOccupation = new TIntArrayList();
                        vertexOccupation.add(tail);
                        occupation.occupyVertex(1, head, new PartialMatching(vertexOccupation));
                    } catch (DomainCheckerException e) {
                        continue;
                    }
                    PathIterator iterator = PathIterator.get(targetGraph, data, tail, head, occupation, () -> 2, settings, () -> {
                        TIntList vertexMatching = new TIntArrayList();
                        vertexMatching.add(tail);
                        vertexMatching.add(head);
                        return new PartialMatching(vertexMatching);
                    });
                    Path path;
                    try {
                        while ((path = iterator.next()) != null) {
                            assert path.asList().size() == new TIntHashSet(path.asList()).size();
                            pathCount.get(strategy).add(new Path(path));
                        }
                    } catch (Exception e) {
                        System.err.println(counter);
                        fail();
                        throw e;
                    }
                }
                assert new HashSet<>(pathCount.values()).size() == 1 : counter + "\n" + myMaptoString(pathCount) + "for:\n" + targetGraph.toString();
            }
        }
    }
}
