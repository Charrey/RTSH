package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;

public class MaxDegreeFirst implements GraphVertexMapper {


    @Override
    public Mapping apply(@NotNull MyGraph graph) {
        int[] oldToNew = new int[graph.vertexSet().size()];
        int[] newToOld = graph.vertexSet().stream().sorted((o1, o2) -> Integer.compare(graph.degreeOf(o2), graph.degreeOf(o1))).mapToInt(x -> x).toArray();
        for (int i = 0; i < newToOld.length; i++) {
            oldToNew[newToOld[i]] = i;
        }
        MyGraph newGraph = MyGraph.applyOrdering(graph, newToOld, oldToNew);
        return new Mapping(newGraph, newToOld);
    }
}
