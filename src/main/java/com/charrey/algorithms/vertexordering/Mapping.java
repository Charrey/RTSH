package com.charrey.algorithms.vertexordering;

import com.charrey.TestCaseProvider;
import com.charrey.graph.MyGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Mapping {
    public final Map<Integer, Integer> newToOld;
    public final Map<Integer, Integer> oldToNew = new HashMap<>();
    public final MyGraph graph;


    public Mapping(MyGraph graph, Map<Integer, Integer> newToOld) {
        this.graph = graph;
        this.newToOld = newToOld;
        newToOld.forEach((key, value) -> oldToNew.put(value, key));
    }
}
