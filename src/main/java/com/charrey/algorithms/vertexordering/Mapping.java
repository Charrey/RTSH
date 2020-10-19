package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;

import java.util.Map;

public class Mapping {
    public final Map<Integer, Integer> newToOld;
    public final MyGraph graph;

    public Mapping(MyGraph graph, Map<Integer, Integer> newToOld) {
        this.graph = graph;
        this.newToOld = newToOld;
    }
}
