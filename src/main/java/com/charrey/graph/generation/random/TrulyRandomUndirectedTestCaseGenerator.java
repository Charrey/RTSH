package com.charrey.graph.generation.random;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;

public class TrulyRandomUndirectedTestCaseGenerator extends TrulyRandomTestCaseGenerator {

    public TrulyRandomUndirectedTestCaseGenerator(int patternNodes, int patternEdges, double nodeFactor, int seed) {
        super(patternNodes, patternEdges, nodeFactor, seed);
    }

    @NotNull
    protected MyGraph randomGraph(int nodes, int edges) {
        GnmRandomGraphGenerator<Vertex, DefaultEdge> gen = new GnmRandomGraphGenerator<>(nodes, edges, random.nextLong());
        MyGraph pattern = new MyGraph(false);
        gen.generateGraph(pattern);
        return pattern;
    }
}
