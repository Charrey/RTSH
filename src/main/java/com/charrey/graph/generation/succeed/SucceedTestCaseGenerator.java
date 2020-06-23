package com.charrey.graph.generation.succeed;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.util.GraphUtil;
import com.charrey.util.Util;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
     * @param patternNodes the number of nodes the source graph should have
     * @param patternEdges the number of edges the source graph should have
     * @param extraRoutingNodes the number of intermediate nodes that should on average be added
     * @param extraNodes        the number of 'distraction' nodes that should be added
     * @param seed              a random seed used to obtain reproducibility
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
        MyGraph targetGraph = GraphUtil.copy(sourceGraph, randomGen);
        insertIntermediateNodes(targetGraph, extraRoutingNodes, randomGen);
        addExtraNodes(targetGraph, extraNodes, patternNodes == 0 ? 0 : patternEdges / (double) patternNodes, randomGen);
        return new TestCase(sourceGraph, targetGraph);
    }

    /**
     * Returns the source graph for some test case.
     * @param patternNodes the number of nodes the graph needs to have.
     * @param patternEdges the number of edges the graph needs to have.
     * @return the source graph.
     */
    protected abstract MyGraph getSource(int patternNodes, int patternEdges);

    private void addExtraNodes(@NotNull MyGraph targetGraph, int extraNodes, double expectedEdges, @NotNull RandomGenerator randomGen) {
        Map<Vertex, Integer> neededEdges = new HashMap<>();
        if (expectedEdges == 0) {
            return;
        }
        IntegerDistribution distribution = new GeometricDistribution(randomGen, 1./(expectedEdges+1));
        for (int i = 0; i < extraNodes; i++) {
            Vertex vertex = targetGraph.addVertex();
            neededEdges.put(vertex, distribution.sample());
        }
        while (!neededEdges.isEmpty()) {
            Vertex randomKey = Util.pickRandom(neededEdges.keySet(), randomGen);
            if (targetGraph.incomingEdgesOf(randomKey).size() + targetGraph.outgoingEdgesOf(randomKey).size() >= neededEdges.get(randomKey)) {
                neededEdges.remove(randomKey);
                continue;
            }
            Set<Vertex> targets = targetGraph.vertexSet().stream().filter(x -> x != randomKey && (!targetGraph.containsEdge(x, randomKey) || !targetGraph.containsEdge(randomKey, x))).collect(Collectors.toSet());
            if (targets.isEmpty()) {
                neededEdges.remove(randomKey);
            } else {
                Vertex target = Util.pickRandom(targets, randomGen);
                Vertex from;
                Vertex to;
                if (random.nextBoolean()) {
                    from = randomKey;
                    to = target;
                } else {
                    to = randomKey;
                    from = target;
                }
                if (targetGraph.containsEdge(from, to)) {
                    Vertex temp = to;
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


}