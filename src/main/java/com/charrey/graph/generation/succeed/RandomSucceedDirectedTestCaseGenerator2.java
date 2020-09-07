package com.charrey.graph.generation.succeed;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.util.GraphUtil;
import guru.nidi.graphviz.model.Link;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jgrapht.generate.GnmRandomGraphGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class that generates random test cases of directed graphs that guarantees a homeomorphism is possible.
 */
public class RandomSucceedDirectedTestCaseGenerator2 extends TestCaseGenerator {


    private final int et;
    private final int vs;
    private final int es;
    private final int vt;
    private final Random random;
    private final boolean labels;

    public RandomSucceedDirectedTestCaseGenerator2(int vs, int es, int vt, int et, long seed, boolean labels) {
        this.vs = vs;
        this.es = es;
        this.vt = vt;
        this.et = et;
        this.random = new Random(seed);
        this.labels = labels;
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
        if (labels) {
            addSourceGraphLabels(sourceGraph);
        }
        GraphUtil.CopyResult copy = GraphUtil.copy(sourceGraph, randomGen);
        Set<Integer> addedArbitrary = insertIntermediateNodes(sourceGraph, copy.graph, 0.5, labels);
        addEdges(copy.graph, addedArbitrary);
        return new TestCase(sourceGraph, copy.graph, null, null);
    }

    private void addSourceGraphLabels(MyGraph sourceGraph) {
        int sourceWires = (int) Math.max(1, Math.round(9d * sourceGraph.vertexSet().size() / 14d));
        int sourceSlices = (int) Math.max(1, Math.round(sourceGraph.vertexSet().size() / 14d));
        List<Integer> vertices = new ArrayList<>(sourceGraph.vertexSet());
        Collections.shuffle(vertices, random);
        for (int i = 0; i < sourceWires; i++) {
            sourceGraph.addAttribute(vertices.get(i), "label", "wire");
        }
        for (int i = sourceWires; i < sourceWires + sourceSlices; i++) {
            sourceGraph.addAttribute(vertices.get(i), "label", "SLICE");
        }
        for (int i = sourceWires + sourceSlices; i < vertices.size(); i++) {
            sourceGraph.addAttribute(vertices.get(i), "label", "arc");
        }
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


    private Set<Integer> insertIntermediateNodes(MyGraph source, MyGraph target, double insertProbability, boolean labels) {
        Set<Integer> addedArbitrary = new HashSet<>();
        LinkedList<String> toAdd = new LinkedList<>();
        if (labels) {

            int sourceWires = (int) source.vertexSet().stream().filter(x -> source.getLabels(x).contains("wire")).count();
            int sourceSlices = (int) source.vertexSet().stream().filter(x -> source.getLabels(x).contains("SLICE")).count();
            int sourceArcs = source.vertexSet().size() - (sourceWires + sourceSlices);
            int targetWires = (int) Math.max(sourceWires, Math.round(414d * vt / 2908d)) - sourceWires;
            int targetPorts = (int) Math.round(124d * vt / 2908d);
            int targetSlices = (int) Math.max(sourceSlices, Math.round(4d * vt / 2908d)) - sourceSlices;
            int targetArcs = vt - (targetPorts + sourceWires + targetWires + sourceSlices + targetSlices + sourceWires + targetWires) - sourceArcs;
            for (int i = 0; i < targetWires; i++) {
                toAdd.add("wire");
            }
            for (int i = 0; i < targetPorts; i++) {
                toAdd.add("port");
            }
            for (int i = 0; i < targetSlices; i++) {
                toAdd.add("SLICE");
            }
            for (int i = 0; i < targetArcs; i++) {
                toAdd.add("arc");
            }
            Collections.shuffle(toAdd, random);
        }
        while (target.vertexSet().size() < vt) {
            int newVertex = target.addVertex();
            if (labels) {
                target.addAttribute(newVertex, "label", toAdd.pollFirst());
            }
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
        assert toAdd.isEmpty();
        return addedArbitrary;
    }


    private MyGraph getSource() {
        GnmRandomGraphGenerator<Integer, MyEdge> gen = new GnmRandomGraphGenerator<>(vs, es, random.nextLong());
        MyGraph pattern = new MyGraph(true);
        gen.generateGraph(pattern);
        return pattern;
    }
}
