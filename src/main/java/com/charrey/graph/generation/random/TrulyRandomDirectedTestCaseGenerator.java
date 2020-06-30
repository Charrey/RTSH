package com.charrey.graph.generation.random;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.generate.GnmRandomGraphGenerator;

/**
 * A class that generates random test cases of directed graphs.
 */
public class TrulyRandomDirectedTestCaseGenerator extends TrulyRandomTestCaseGenerator {

    /**
     * Instantiates a new generator.
     *
     * @param patternNodes the number of nodes the source graph should have
     * @param patternEdges the number of edges the source graph should have
     * @param nodeFactor   factor of how much larger the target graph should be
     * @param seed         a random seed used to obtain reproducibility.
     */
    public TrulyRandomDirectedTestCaseGenerator(int patternNodes, int patternEdges, double nodeFactor, int seed) {
        super(patternNodes, patternEdges, nodeFactor, seed);
    }

    @NotNull
    protected MyGraph randomGraph(int nodes, int edges) {
        GnmRandomGraphGenerator<Integer, MyEdge> gen = new GnmRandomGraphGenerator<>(nodes, edges, random.nextLong());
        MyGraph pattern = new MyGraph(true);
        gen.generateGraph(pattern);
        return pattern;
    }
}
