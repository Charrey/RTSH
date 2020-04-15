package com.charrey.graph.generation;

import com.charrey.graph.RoutingVertexTable;
import com.charrey.graph.Vertex;
import com.charrey.util.AnyGenerator;
import com.charrey.util.GraphUtil;
import com.charrey.util.Util;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.Collectors;

public class TestCaseGenerator {



    public static Pair<GraphGeneration, GraphGeneration> getRandom(int patternNodes, int patternEdges, int extraRoutingNodes, int extraNodes, long seed) {
        Random random = new Random(seed);
        final RandomGenerator randomGen = new AbstractRandomGenerator() {
            @Override
            public void setSeed(long seed) {
                random.setSeed(seed);
            }

            @Override
            public double nextDouble() {
                return random.nextDouble();
            }
        };
        Graph<Vertex, DefaultEdge> pattern = getPattern(patternNodes, patternEdges, seed);
        Graph<Vertex, DefaultEdge> targetGraph = GraphUtil.copy(pattern, random);
        insertIntermediateNodes(targetGraph, extraRoutingNodes, randomGen);
        addExtraNodes(targetGraph, extraNodes, patternEdges / (double) patternNodes, randomGen);
        return new Pair<>(new GraphGeneration(pattern, new RoutingVertexTable()), new GraphGeneration(targetGraph, new RoutingVertexTable()));
    }

    private static void addExtraNodes(Graph<Vertex, DefaultEdge> targetGraph, int extraNodes, double expectedEdges, RandomGenerator randomGen) {
        Map<Vertex, Integer> neededEdges = new HashMap<>();
        Map<Vertex, Integer> actualEdges = new HashMap<>();
        IntegerDistribution distribution = new GeometricDistribution(randomGen, 1./(expectedEdges+1));
        for (int i = 0; i < extraNodes; i++) {
            Vertex vertex = targetGraph.addVertex();
            neededEdges.put(vertex, distribution.sample());
            actualEdges.put(vertex, 0);
        }
        while (!neededEdges.isEmpty()) {
            Vertex randomKey = Util.pickRandom(neededEdges.keySet(), new RandomAdaptor(randomGen));
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
                Vertex target = Util.pickRandom(targets, new RandomAdaptor(randomGen));
                targetGraph.addEdge(randomKey, target);
            }
        }
    }

    private static void insertIntermediateNodes(Graph<Vertex, DefaultEdge> targetGraph, int extraRoutingNodes, RandomGenerator random) {
        IntegerDistribution distribution = new GeometricDistribution(random, 1./(extraRoutingNodes + 1));
        for (DefaultEdge edge : new HashSet<>(targetGraph.edgeSet())) {
            int toAdd = distribution.sample();
            while (toAdd > 0) {
                Vertex source = targetGraph.getEdgeSource(edge);
                Vertex target = targetGraph.getEdgeTarget(edge);
                targetGraph.removeEdge(source, target);
                Vertex intermediate = targetGraph.addVertex();
                targetGraph.addEdge(intermediate, target);
                edge = targetGraph.addEdge(source, intermediate);
                toAdd -= 1;
            }
        }
    }

    private static Graph<Vertex, DefaultEdge> getPattern(int patternNodes, int patternEdges, long seed) {
        GnmRandomGraphGenerator<Vertex, DefaultEdge> gen = new GnmRandomGraphGenerator<>(patternNodes, patternEdges, seed + 17);
        AnyGenerator<Integer> numbers = new AnyGenerator<>(0, x -> ++x);
        Graph<Vertex, DefaultEdge> pattern = new SimpleGraph<>(() -> new Vertex(numbers.get()), DefaultEdge::new, false);
        gen.generateGraph(pattern);
        return pattern;
    }
}
