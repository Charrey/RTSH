package com.charrey.graph.generation;

import java.io.Serializable;

public class TestCase implements Serializable {
    public final MyGraph targetGraph;
    public final MyGraph sourceGraph;

    public TestCase(MyGraph sourceGraph, MyGraph targetGraph) {
        this.sourceGraph = sourceGraph;
        this.targetGraph = targetGraph;
    }
}
