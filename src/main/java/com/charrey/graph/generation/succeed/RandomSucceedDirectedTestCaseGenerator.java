package com.charrey.graph.generation.succeed;

import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;

/**
 * A class that generates random test cases of directed graphs that guarantees a homeomorphism is possible.
 */
public class RandomSucceedDirectedTestCaseGenerator extends SucceedTestCaseGenerator {


    /**
     * Instantiates a new Random succeed directed test case generator.
     *
     * @param patternNodes      the number of nodes the source graph should have
     * @param patternEdges      the number of edges the source graph should have
     * @param extraRoutingNodes the number of intermediate nodes that should on average be added
     * @param extraNodes        the number of 'distraction' nodes that should be added
     * @param seed              a random seed used to obtain reproducibility
     */
    public RandomSucceedDirectedTestCaseGenerator(int patternNodes, int patternEdges, double extraRoutingNodes, int extraNodes, long seed) {
        super(seed, patternNodes, patternEdges, extraRoutingNodes, extraNodes);
    }


    @Override
    @NotNull
    protected MyGraph getSource(int patternNodes, int patternEdges) {
        GnmRandomGraphGenerator<Integer, DefaultEdge> gen = new GnmRandomGraphGenerator<>(patternNodes, patternEdges, random.nextLong());
        MyGraph pattern = new MyGraph(true);
        gen.generateGraph(pattern);
        return pattern;
    }
}
