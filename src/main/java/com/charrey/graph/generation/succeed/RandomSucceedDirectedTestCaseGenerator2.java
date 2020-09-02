package com.charrey.graph.generation.succeed;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.util.GraphUtil;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jgrapht.generate.GnmRandomGraphGenerator;

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.Set;

/**
 * A class that generates random test cases of directed graphs that guarantees a homeomorphism is possible.
 */
public class RandomSucceedDirectedTestCaseGenerator2 extends TestCaseGenerator {


    private final int et;
    private final int vs;
    private final int es;
    private final int vt;
    private final Random random;

    public RandomSucceedDirectedTestCaseGenerator2(int vs, int es, int vt, int et, long seed) {
        this.vs = vs;
        this.es = es;
        this.vt = vt;
        this.et = et;
        this.random = new Random(seed);
    }

    @Override
    public void makeHarder() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected TestCase getRandom() {
        final RandomGenerator randomGen = new Well512a();
        long newSeed = random.nextLong();
        randomGen.setSeed(newSeed);
        MyGraph sourceGraph = getSource();
        GraphUtil.CopyResult copy = GraphUtil.copy(sourceGraph, randomGen);
        Set<Integer> addedArbitrary = insertIntermediateNodes(copy.graph, 0.5);
        addEdges(copy.graph, addedArbitrary);
        return new TestCase(sourceGraph, copy.graph, null, null);
    }

    private void addEdges(MyGraph graph, Set<Integer> addedArbitrary) {
        while (!addedArbitrary.isEmpty() && graph.edgeSet().size() < et && averageDegree(graph, addedArbitrary) < averageDegree(graph, graph.vertexSet())) {
            int randomArbitraryVertex = addedArbitrary.stream().skip(random.nextInt(addedArbitrary.size())).findFirst().get();
            int randomOtherVertex = graph.vertexSet().stream().skip(random.nextInt(graph.vertexSet().size())).findFirst().get();
            if (random.nextBoolean()) {
                graph.addEdge(randomArbitraryVertex, randomOtherVertex);
            } else {
                graph.addEdge(randomOtherVertex, randomArbitraryVertex);
            }
        }
        while (graph.edgeSet().size() < et) {
            int randomArbitraryVertex = graph.vertexSet().stream().skip(random.nextInt(graph.vertexSet().size())).findFirst().get();
            int randomOtherVertex =     graph.vertexSet().stream().skip(random.nextInt(graph.vertexSet().size())).findFirst().get();
            graph.addEdge(randomArbitraryVertex, randomOtherVertex);
        }
    }

    private double averageDegree(MyGraph graph, Set<Integer> vertices) {
        OptionalDouble toReturn =  vertices.stream().mapToInt(graph::degreeOf).average();
        if (toReturn.isEmpty()) {
            throw new UnsupportedOperationException();
        } else {
            return toReturn.getAsDouble();
        }
    }


    private Set<Integer> insertIntermediateNodes(MyGraph target, double insertProbability) {
        Set<Integer> addedArbitrary = new HashSet<>();
        while (target.vertexSet().size() < vt) {
            int newVertex = target.addVertex();
            if (random.nextDouble() < insertProbability) {
                MyEdge randomEdge = target.edgeSet().stream().skip(random.nextInt(target.edgeSet().size())).findFirst().get();
                int sourceVertex = target.getEdgeSource(randomEdge);
                int targetVertex = target.getEdgeTarget(randomEdge);
                target.removeEdge(randomEdge);
                target.addEdge(sourceVertex, newVertex);
                target.addEdge(newVertex, targetVertex);

            } else {
                addedArbitrary.add(newVertex);
            }
        }
        return addedArbitrary;
    }


    private MyGraph getSource() {
        GnmRandomGraphGenerator<Integer, MyEdge> gen = new GnmRandomGraphGenerator<>(vs, es, random.nextLong());
        MyGraph pattern = new MyGraph(true);
        gen.generateGraph(pattern);
        return pattern;
    }
}
