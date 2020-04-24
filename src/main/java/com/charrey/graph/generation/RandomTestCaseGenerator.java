package com.charrey.graph.generation;

import com.charrey.graph.RoutingVertexTable;
import com.charrey.graph.Vertex;
import com.charrey.util.GraphUtil;
import com.charrey.util.Util;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jgrapht.Graph;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.Collectors;

public class RandomTestCaseGenerator {

    private final Random random;
    private final int patternNodes;
    private final int patternEdges;
    private final double extraRoutingNodes;
    private final int extraNodes;

    public RandomTestCaseGenerator(int patternNodes, int patternEdges, double extraRoutingNodes, int extraNodes) {
        this(patternNodes, patternEdges, extraRoutingNodes, extraNodes, System.currentTimeMillis());
    }

    public RandomTestCaseGenerator(int patternNodes, int patternEdges, double extraRoutingNodes, int extraNodes, long seed) {
        this.patternNodes = patternNodes;
        this.patternEdges = patternEdges;
        this.extraRoutingNodes = extraRoutingNodes;
        this.extraNodes = extraNodes;
        this.random = new Random(seed);
    }

    private final Deque<TestCase> testCases = new ArrayDeque<>();
    public void init(int amount) {
        testCases.clear();
        System.out.println("Generating graphs..");
        for (int i = 0; i < amount; i++) {
            testCases.add(getRandom());
            System.out.println(i + "/" + amount);
        }
    }

    public TestCase getNext() {
        return testCases.pop();
    }


    public TestCase getRandom() {
        final RandomGenerator randomGen = new Well512a();
        randomGen.setSeed(random.nextLong());
        Graph<Vertex, DefaultEdge> pattern = getPattern(patternNodes, patternEdges, randomGen.nextLong());
        Graph<Vertex, DefaultEdge> targetGraph = GraphUtil.copy(pattern, randomGen);
        insertIntermediateNodes(targetGraph, extraRoutingNodes, randomGen);
        addExtraNodes(targetGraph, extraNodes, patternEdges / (double) patternNodes, randomGen);
        return new TestCase(new GraphGeneration(pattern, new RoutingVertexTable()), new GraphGeneration(targetGraph, new RoutingVertexTable()));
    }



    private Graph<Vertex, DefaultEdge> getPattern(int patternNodes, int patternEdges, long seed) {
        GnmRandomGraphGenerator<Vertex, DefaultEdge> gen = new GnmRandomGraphGenerator<>(patternNodes, patternEdges, seed + 17);
        Graph<Vertex, DefaultEdge> pattern = new SimpleGraph<>(new GraphGenerator.IntGenerator(), new GraphGenerator.BasicEdgeSupplier(), false);
        gen.generateGraph(pattern);
        for (Vertex v : pattern.vertexSet()) {
            v.setGraph(pattern);
        }
        return pattern;
    }

    private void insertIntermediateNodes(Graph<Vertex, DefaultEdge> targetGraph, double extraRoutingNodes, RandomGenerator random) {
        IntegerDistribution distribution = new GeometricDistribution(random, 1./(extraRoutingNodes + 1));
        for (DefaultEdge edge : new HashSet<>(targetGraph.edgeSet())) {
            int toAdd = distribution.sample();
            while (toAdd > 0) {
                Vertex source = targetGraph.getEdgeSource(edge);
                Vertex target = targetGraph.getEdgeTarget(edge);
                targetGraph.removeEdge(source, target);
                Vertex intermediate = targetGraph.addVertex();
                intermediate.setGraph(targetGraph);
                targetGraph.addEdge(intermediate, target);
                edge = targetGraph.addEdge(source, intermediate);
                toAdd -= 1;
            }
        }
    }

    private void addExtraNodes(Graph<Vertex, DefaultEdge> targetGraph, int extraNodes, double expectedEdges, RandomGenerator randomGen) {
        Map<Vertex, Integer> neededEdges = new HashMap<>();
        Map<Vertex, Integer> actualEdges = new HashMap<>();
        IntegerDistribution distribution = new GeometricDistribution(randomGen, 1./(expectedEdges+1));
        for (int i = 0; i < extraNodes; i++) {
            Vertex vertex = targetGraph.addVertex();
            vertex.setGraph(targetGraph);
            neededEdges.put(vertex, distribution.sample());
            actualEdges.put(vertex, 0);
        }
        while (!neededEdges.isEmpty()) {
            Vertex randomKey = Util.pickRandom(neededEdges.keySet(), randomGen);
            if (actualEdges.get(randomKey) >= neededEdges.get(randomKey)) {
                actualEdges.remove(randomKey);
                neededEdges.remove(randomKey);
                continue;
            }
            Set<Vertex> targets = targetGraph.vertexSet().stream().filter(x -> x != randomKey && !targetGraph.containsEdge(x, randomKey)).collect(Collectors.toSet());
            if (targets.isEmpty()) {
                neededEdges.remove(randomKey);
                actualEdges.remove(randomKey);
            } else {
                Vertex target = Util.pickRandom(targets, randomGen);
                targetGraph.addEdge(randomKey, target);
            }
        }
    }




    public static class TestCase {
        public final GraphGeneration target;
        public final GraphGeneration source;

        public TestCase(GraphGeneration source, GraphGeneration target) {
            this.source = source;
            this.target = target;
        }
    }
}
