package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;

public class MaxDegreeFirst extends GraphVertexMapper {


    @Override
    public Mapping apply(@NotNull MyGraph graph) {
        int[] old_to_new = new int[graph.vertexSet().size()];
        int[] new_to_old = graph.vertexSet().stream().sorted((o1, o2) -> Integer.compare(graph.degreeOf(o2), graph.degreeOf(o1))).mapToInt(x -> x).toArray();
        for (int i = 0; i < new_to_old.length; i++) {
            old_to_new[new_to_old[i]] = i;
        }
        MyGraph newGraph = MyGraph.applyOrdering(graph, new_to_old, old_to_new);
        return new Mapping(newGraph, new_to_old);
    }
}
