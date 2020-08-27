package com.charrey.graph.generation;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.util.GraphUtil;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A wrapper class for a source graph and a target graph to be used for finding a homeomorphism.
 */
public class TestCase implements Iterable<MyGraph> {
    /**
     * The target graph in which the subgraph homeomorphism is found
     */
    private final MyGraph targetGraph;
    /**
     * The source graph to embed in the target graph
     */
    private final MyGraph sourceGraph;

    private final transient RandomGenerator random = new Well512a(1888939);

    private final int[] expectedVertexMatching;
    private final Map<MyEdge, Path> expectedEdgeMatching;

    /**
     * Creates a new Test case.
     *  @param sourceGraph the source graph
     * @param targetGraph the target graph
     * @param expectedVertexMatching
     * @param expectedEdgeMatching
     */
    public TestCase(MyGraph sourceGraph, MyGraph targetGraph, int[] expectedVertexMatching, Map<MyEdge, Path> expectedEdgeMatching) {
        this.expectedVertexMatching = expectedVertexMatching == null ? null : expectedVertexMatching.clone();
        this.expectedEdgeMatching = expectedEdgeMatching == null ? null : Collections.unmodifiableMap(expectedEdgeMatching);
        try {
            sourceGraph.randomizeWeights();
            sourceGraph.lock();
        } catch (IllegalStateException ignored) {}
        try {
            targetGraph.randomizeWeights();
            targetGraph.lock();
        } catch (IllegalStateException ignored) {}
        this.sourceGraph = sourceGraph;
        this.targetGraph = targetGraph;
    }

    public MyGraph getSourceGraph() {
        return sourceGraph;
    }

    public MyGraph getTargetGraph() {
        return targetGraph;
    }

    @NotNull
    @Override
    public Iterator<MyGraph> iterator() {
        return List.of(sourceGraph, targetGraph).iterator();
    }

    public synchronized TestCase copy() {
        MyGraph sourceGraphCopy = GraphUtil.copy(this.sourceGraph, random).graph;
        MyGraph targetGraphCopy = GraphUtil.copy(this.targetGraph, random).graph;
        int[] expectedVertexMatchingCopy = this.expectedVertexMatching == null ? null : Arrays.copyOf(this.expectedVertexMatching, this.expectedVertexMatching.length);
        Map<MyEdge, Path> expectedEdgeMatchingCopy = this.expectedEdgeMatching == null ? null : this.expectedEdgeMatching.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, myEdgePathEntry -> new Path(myEdgePathEntry.getValue())));
        return new TestCase(
                sourceGraphCopy,
                targetGraphCopy,
                expectedVertexMatchingCopy,
                expectedEdgeMatchingCopy);
    }

    public int[] getExpectedVertexMatching() {
        return expectedVertexMatching;
    }
}
