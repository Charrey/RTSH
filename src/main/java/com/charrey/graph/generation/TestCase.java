package com.charrey.graph.generation;

import com.charrey.graph.MyGraph;
import com.charrey.util.GraphUtil;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * A wrapper class for a source graph and a target graph to be used for finding a homeomorphism.
 */
public class TestCase implements Serializable, Iterable<MyGraph> {
    /**
     * The target graph in which the subgraph homeomorphism is found
     */
    private final MyGraph targetGraph;
    /**
     * The source graph to embed in the target graph
     */
    private final MyGraph sourceGraph;

    private final RandomGenerator random = new Well512a(1888939);

    /**
     * Creates a new Test case.
     *
     * @param sourceGraph the source graph
     * @param targetGraph the target graph
     */
    public TestCase(MyGraph sourceGraph, MyGraph targetGraph) {
        sourceGraph.randomizeWeights();
        sourceGraph.lock();
        targetGraph.randomizeWeights();
        targetGraph.lock();
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
        return new TestCase(GraphUtil.copy(sourceGraph, random), GraphUtil.copy(targetGraph, random));
    }
}
