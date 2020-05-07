package com.charrey.graph.generation;

import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.Random;

public class TrulyRandomTestCaseGenerator extends TestCaseGenerator {

    private final double nodeFactor;
    private int patternNodes;
    private int patternEdges;
    private int targetNodes;
    private int targetEdges;
    private final Random random;


    public TrulyRandomTestCaseGenerator(int patternNodes, int patternEdges, double nodeFactor, int seed) {
        this.patternNodes = patternNodes;
        this.patternEdges = patternEdges;
        this.targetNodes = (int) Math.ceil(patternNodes * nodeFactor);
        this.targetEdges = (int) Math.ceil(targetNodes * patternEdges / (double) patternNodes);
        random = new Random(seed);
        this.nodeFactor = nodeFactor;
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

    @Override
    protected TestCase getRandom() {
        GraphGeneration patternGraph = new GraphGeneration(randomGraph(patternNodes, patternEdges), null);
        GraphGeneration targetGraph = new GraphGeneration(randomGraph(targetNodes, targetEdges), null);
        return new TestCase(patternGraph, targetGraph);
    }

    private Graph<Vertex, DefaultEdge> randomGraph(int nodes, int edges) {
        GnmRandomGraphGenerator<Vertex, DefaultEdge> gen = new GnmRandomGraphGenerator<>(nodes, edges, random.nextLong());
        Graph<Vertex, DefaultEdge> pattern = new SimpleGraph<>(new GraphGenerator.IntGenerator(), new GraphGenerator.BasicEdgeSupplier(), false);
        gen.generateGraph(pattern);
        return pattern;
    }
}
