package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;

public class Mapping {
    public final int[] new_to_old;
    public final MyGraph graph;

    Mapping(MyGraph graph, int[] new_to_old) {
        this.graph = graph;
        this.new_to_old = new_to_old;
    }
}
