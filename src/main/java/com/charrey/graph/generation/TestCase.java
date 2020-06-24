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
    public final MyGraph targetGraph;
    /**
     * The source graph to embed in the target graph
     */
    public MyGraph sourceGraph;

    /**
     * Creates a new Test case.
     *
     * @param sourceGraph the source graph
     * @param targetGraph the target graph
     */
    public TestCase(MyGraph sourceGraph, MyGraph targetGraph) {
        this.sourceGraph = sourceGraph;
        this.targetGraph = targetGraph;
    }

    @NotNull
    @Override
    public Iterator<MyGraph> iterator() {
       return List.of(sourceGraph, targetGraph).iterator();
    }
}
