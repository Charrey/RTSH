package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MaxDegreeFirst implements GraphVertexMapper {


    @Override
    public Mapping apply(@NotNull MyGraph graph) {
        int[] oldToNew = new int[graph.vertexSet().size()];
        int[] newToOld = graph.vertexSet().stream().sorted((o1, o2) -> Integer.compare(graph.degreeOf(o2), graph.degreeOf(o1))).mapToInt(x -> x).toArray();
        for (int i = 0; i < newToOld.length; i++) {
            oldToNew[newToOld[i]] = i;
        }
        Map<Integer, Integer> newToOldMap = new HashMap<>();
        for (int i = 0; i < newToOld.length; i++) {
            newToOldMap.put(i, newToOld[i]);
        }
        MyGraph newGraph = MyGraph.applyOrdering(graph, newToOldMap, oldToNew);
        return new Mapping(newGraph, newToOldMap);
    }
}
