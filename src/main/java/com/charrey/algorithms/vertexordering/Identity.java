package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;

public class Identity implements GraphVertexMapper{

    @Override
    public Mapping apply(@NotNull MyGraph graph) {
        int[] oldToNew = new int[graph.vertexSet().size()];
        for (int i = 0; i < oldToNew.length; i++) {
            oldToNew[i] = i;
        }
        MyGraph newGraph = MyGraph.applyOrdering(graph, oldToNew, oldToNew);
        return new Mapping(newGraph, oldToNew);
    }
}
