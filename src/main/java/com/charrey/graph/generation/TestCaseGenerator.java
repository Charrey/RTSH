package com.charrey.graph.generation;

import com.charrey.graph.MyGraph;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;

public abstract class TestCaseGenerator {

    protected static void insertIntermediateNodes(@NotNull MyGraph targetGraph, double extraRoutingNodes, RandomGenerator random) {
        IntegerDistribution distribution = new GeometricDistribution(random, 1./(extraRoutingNodes + 1));
        for (DefaultEdge edge : new HashSet<>(targetGraph.edgeSet())) {
            int toAdd = distribution.sample();
            while (toAdd > 0) {
                int source = targetGraph.getEdgeSource(edge);
                int target = targetGraph.getEdgeTarget(edge);
                targetGraph.removeEdge(source, target);
                int intermediate = targetGraph.addVertex();
                targetGraph.addEdge(intermediate, target);
                edge = targetGraph.addEdge(source, intermediate);
                toAdd -= 1;
            }
        }
    }

    private final Deque<TestCase> testCases = new ArrayDeque<>();

    public TestCase getNext() {
        return testCases.pop();
    }

    public boolean hasNext() {
        return !testCases.isEmpty();
    }

    public void init(int amount, boolean print) {
        testCases.clear();
        if (print) {
            System.out.println("Generating graphs..");
        }
        for (int i = 0; i < amount; i++) {
            testCases.add(getRandom());
            if (print) {
                System.out.println(i + "/" + amount);
            }
        }
    }

    public abstract void makeHarder();

    protected abstract TestCase getRandom();
}
