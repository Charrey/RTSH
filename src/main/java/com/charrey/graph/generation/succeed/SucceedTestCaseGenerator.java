package com.charrey.graph.generation.succeed;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.util.GraphUtil;
import com.charrey.util.Util;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A test case generator that guarantees a solution exists.
 */
public abstract class SucceedTestCaseGenerator extends TestCaseGenerator {


    /**
     * Random object that has to be used for all non-deterministic operations.
     */
    protected final Random random;
    private int patternNodes;
    private int patternEdges;
    private final double extraRoutingNodes;
    private final int extraNodes;

    /**
     * Instantiates a new Random succeed test case generator.
     *
     * @param seed              a random seed used to obtain reproducibility
     * @param patternNodes      the number of nodes the source graph should have
     * @param patternEdges      the number of edges the source graph should have
     * @param extraRoutingNodes the number of intermediate nodes that should on average be added
     * @param extraNodes        the number of 'distraction' nodes that should be added
     */
    SucceedTestCaseGenerator(long seed, int patternNodes, int patternEdges, double extraRoutingNodes, int extraNodes) {
        this.random = new Random(seed);
        this.patternNodes = patternNodes;
        this.patternEdges = patternEdges;
        this.extraRoutingNodes = extraRoutingNodes;
        this.extraNodes = extraNodes;
    }

    @NotNull
    public TestCase getRandom() {
        final RandomGenerator randomGen = new Well512a();
        randomGen.setSeed(random.nextLong());
        MyGraph sourceGraph = getSource(patternNodes, patternEdges);
        GraphUtil.CopyResult copy = GraphUtil.copy(sourceGraph, randomGen);
        insertIntermediateNodes(sourceGraph, copy, extraRoutingNodes, randomGen);
        addExtraNodes(copy.graph, extraNodes, patternNodes == 0 ? 0 : patternEdges / (double) patternNodes, randomGen);
        return new TestCase(sourceGraph, copy.graph, copy.vertexMatching, copy.edgeMatching);
    }

    /**
     * Returns the source graph for some test case.
     *
     * @param patternNodes the number of nodes the graph needs to have.
     * @param patternEdges the number of edges the graph needs to have.
     * @return the source graph.
     */
    protected abstract MyGraph getSource(int patternNodes, int patternEdges);

    private void addExtraNodes(@NotNull MyGraph targetGraph, int extraNodes, double expectedEdges, @NotNull RandomGenerator randomGen) {
        Map<Integer, Integer> neededEdges = new HashMap<>();
        if (expectedEdges == 0) {
            return;
        }
        IntegerDistribution distribution = new GeometricDistribution(randomGen, 1./(expectedEdges+1));
        for (int i = 0; i < extraNodes; i++) {
            int vertex = targetGraph.addVertex();
            neededEdges.put(vertex, distribution.sample());
        }
        while (!neededEdges.isEmpty()) {
            int randomKey = Util.selectRandom(neededEdges.keySet(), x -> true, randomGen);
            if (targetGraph.incomingEdgesOf(randomKey).size() + targetGraph.outgoingEdgesOf(randomKey).size() >= neededEdges.get(randomKey)) {
                neededEdges.remove(randomKey);
                continue;
            }
            Set<Integer> targets = targetGraph.vertexSet().stream().filter(x -> x != randomKey && (!targetGraph.containsEdge(x, randomKey) || !targetGraph.containsEdge(randomKey, x))).collect(Collectors.toSet());
            if (targets.isEmpty()) {
                neededEdges.remove(randomKey);
            } else {
                int target = Util.selectRandom(targets, x -> true, randomGen);
                int from;
                int to;
                if (random.nextBoolean()) {
                    from = randomKey;
                    to = target;
                } else {
                    to = randomKey;
                    from = target;
                }
                if (targetGraph.containsEdge(from, to)) {
                    int temp = to;
                    to = from;
                    from = temp;
                }
                targetGraph.addEdge(from, to);
            }
        }
    }


    @Override
    public void makeHarder() {
        if (patternEdges == (patternNodes * (patternNodes - 1))/2) {
            patternEdges = 0;
            patternNodes++;
        } else {
            patternEdges++;
        }
    }


    private static void insertIntermediateNodes(MyGraph sourceGraph, @NotNull GraphUtil.CopyResult targetGraph, double extraRoutingNodes, RandomGenerator random) {
        IntegerDistribution distribution = new GeometricDistribution(random, 1./(extraRoutingNodes + 1));
        for (MyEdge edge : new HashSet<>(targetGraph.graph.edgeSet())) {
            int toAdd = distribution.sample();
            while (toAdd > 0) {
                int source = targetGraph.graph.getEdgeSource(edge);
                int target = targetGraph.graph.getEdgeTarget(edge);
                targetGraph.graph.removeEdge(source, target);
                int intermediate = targetGraph.graph.addVertex();
                targetGraph.graph.addEdge(intermediate, target);
                edge = targetGraph.graph.addEdge(source, intermediate);
                toAdd -= 1;
                List<Pair<MyEdge, Integer>> matches = sourceGraph.edgeSet().stream().map(x -> new Pair<>(x, Util.indexOfSubList(targetGraph.edgeMatching.get(x).asList().toArray(), new int[]{source, target}))).collect(Collectors.toList());
                matches.forEach(i -> {
                    if (i.getSecond() >= 0) {
                        assert targetGraph.edgeMatching.get(i.getFirst()).getGraph() == targetGraph.graph;
                        targetGraph.edgeMatching.get(i.getFirst()).insert(i.getSecond() + 1, intermediate);
                    }
                });
            }
        }
    }


}
