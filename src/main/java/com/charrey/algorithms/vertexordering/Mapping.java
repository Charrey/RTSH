package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;

public class Mapping {
    public final int[] newToOld;
    public final MyGraph graph;

    Mapping(MyGraph graph, int[] newToOld) {
        this.graph = graph;
        this.newToOld = newToOld;
    }
}
