package com.charrey.graph.generation;

import com.charrey.graph.MyGraph;
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
    private MyGraph sourceGraph;
    private boolean randomizedSourceGraph = false;
    private boolean randomizedTargetGraph = false;

    /**
     * Creates a new Test case.
     *
     * @param sourceGraph the source graph
     * @param targetGraph the target graph
     */
    public TestCase(MyGraph sourceGraph, MyGraph targetGraph) {
        sourceGraph.lock();
        targetGraph.lock();
        this.sourceGraph = sourceGraph;
        this.targetGraph = targetGraph;
    }

    public MyGraph getSourceGraph() {
        if (!randomizedSourceGraph) {
            sourceGraph.randomizeWeights();
            randomizedSourceGraph = true;
        }
        return sourceGraph;
    }

    public void setSourceGraph(MyGraph sourceGraph) {
        this.sourceGraph = sourceGraph;
    }

    public MyGraph getTargetGraph() {
        if (!randomizedTargetGraph) {
            targetGraph.randomizeWeights();
            randomizedTargetGraph = true;
        }
        return targetGraph;
    }

    @NotNull
    @Override
    public Iterator<MyGraph> iterator() {
        return List.of(sourceGraph, targetGraph).iterator();
    }
}
