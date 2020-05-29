package com.charrey.graph.generation;

import com.charrey.graph.Vertex;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;

import java.util.Random;

public class TrulyRandomUndirectedTestCaseGenerator extends TestCaseGenerator {

    private final double nodeFactor;
    private int patternNodes;
    private int patternEdges;
    private int targetNodes;
    private int targetEdges;
    private final Random random;


    public TrulyRandomUndirectedTestCaseGenerator(int patternNodes, int patternEdges, double nodeFactor, int seed) {
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
        MyGraph patternGraph = randomGraph(patternNodes, patternEdges);
        MyGraph targetGraph = randomGraph(targetNodes, targetEdges);
        return new TestCase(patternGraph, targetGraph);
    }

    private MyGraph randomGraph(int nodes, int edges) {
        GnmRandomGraphGenerator<Vertex, DefaultEdge> gen = new GnmRandomGraphGenerator<>(nodes, edges, random.nextLong());
       MyGraph pattern = new MyGraph(false);
        gen.generateGraph(pattern);
        return pattern;
    }
}
