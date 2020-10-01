package com.charrey.graph.generation.random;

import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * A test case generator that does not guarantee a solution exists.
 */
public abstract class TrulyRandomTestCaseGenerator extends TestCaseGenerator {

    private final double nodeFactor;
    /**
     * Random object that has to be used for all non-deterministic operations.
     */
    protected final Random random;
    protected int targetEdges;
    protected int targetNodes;
    protected int patternEdges;
    protected int patternNodes;

    /**
     * Instantiates a new test case generator.
     *
     * @param patternNodes the number of nodes the source graph should have
     * @param patternEdges the number of edges the source graph should have
     * @param nodeFactor   factor of how much larger the target graph should be
     * @param seed         a random seed used to obtain reproducibility.
     */
    TrulyRandomTestCaseGenerator(int patternNodes, int patternEdges, double nodeFactor, int seed) {
        this.patternNodes = patternNodes;
        this.patternEdges = patternEdges;
        this.targetNodes = (int) Math.ceil(patternNodes * nodeFactor);
        this.targetEdges = (int) Math.ceil(targetNodes * patternEdges / (double) patternNodes);
        this.random = new Random(seed);
        this.nodeFactor = nodeFactor;
    }

    @NotNull
    @Override
    protected TestCase getRandom() {
        MyGraph patternGraph = randomGraph(patternNodes, patternEdges, true);
        MyGraph targetGraph = randomGraph(targetNodes, targetEdges, false);
        return new TestCase(patternGraph, targetGraph, null, null);
    }

    @Override
    public void makeHarder() {
        if (patternEdges == (patternNodes * (patternNodes - 1))/2) {
            patternEdges = 0;
            patternNodes++;
        } else {
            patternEdges++;
        }
        this.targetNodes = (int) Math.ceil(patternNodes * nodeFactor);
        this.targetEdges = (int) Math.ceil(targetNodes * patternEdges / (double) patternNodes);
    }

    /**
     * Returns a random graph.
     *
     * @param nodes the number of nodes the graph needs to have.
     * @param edges the number of edges the graph needs to have.
     * @return a random graph with the correct number of nodes and edges.
     */
    protected abstract MyGraph randomGraph(int nodes, int edges, boolean source);


    protected double getNodeFactor() {
        return nodeFactor;
    }
}