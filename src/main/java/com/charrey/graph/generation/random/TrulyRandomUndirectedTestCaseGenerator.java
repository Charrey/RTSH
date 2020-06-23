package com.charrey.graph.generation.random;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;

/**
 * A class that generates random test cases of undirected graphs.
 */
public class TrulyRandomUndirectedTestCaseGenerator extends TrulyRandomTestCaseGenerator {

    /**
     * Instantiates a new generator.
     *
     * @param patternNodes the number of nodes the source graph should have
     * @param patternEdges the number of edges the source graph should have
     * @param nodeFactor   factor of how much larger the target graph should be
     * @param seed         a random seed used to obtain reproducibility.
     */
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
