//package com.charrey.algorithms.vertexordering;
//
//import com.charrey.graph.MyGraph;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class Identity implements GraphVertexMapper{
//
//    @Override
//    public Mapping apply(@NotNull MyGraph graph) {
//        Map<Integer, Integer> oldToNew = new HashMap<>();
//        for (int i = 0; i < graph.vertexSet().size(); i++) {
//            oldToNew.put(i, i);
//        }
//        MyGraph newGraph = MyGraph.applyOrdering(graph, oldToNew, null);
//        return new Mapping(newGraph, oldToNew);
//    }
//}
