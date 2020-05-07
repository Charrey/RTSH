package com.charrey.graph.generation;

import java.io.Serializable;

public class TestCase implements Serializable {
    public final GraphGeneration target;
    public final GraphGeneration source;

    public TestCase(GraphGeneration source, GraphGeneration target) {
        this.source = source;
        this.target = target;
    }
}
