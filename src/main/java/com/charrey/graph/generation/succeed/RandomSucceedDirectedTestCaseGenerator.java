package com.charrey.graph.generation.succeed;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;

public class RandomSucceedDirectedTestCaseGenerator extends SucceedTestCaseGenerator {


    public RandomSucceedDirectedTestCaseGenerator(int patternNodes, int patternEdges, double extraRoutingNodes, int extraNodes, long seed) {
        super(seed, patternNodes, patternEdges, extraRoutingNodes, extraNodes);
    }


    @Override
    @NotNull
    protected MyGraph getPattern(int patternNodes, int patternEdges, long seed) {
        GnmRandomGraphGenerator<Vertex, DefaultEdge> gen = new GnmRandomGraphGenerator<>(patternNodes, patternEdges, seed + 17);
        MyGraph pattern = new MyGraph(true);
        gen.generateGraph(pattern);
        return pattern;
    }
}
