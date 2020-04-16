package com.charrey;

import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class Occupation {

    private final BitSet bits;

    private Occupation(int size){
        this.bits = new BitSet(size);
    }

    public void occupy(Vertex v) {
        assert !bits.get(v.intData());
        bits.set(v.intData());
    }

    public void release(Vertex v) {
        assert bits.get(v.intData());
        bits.clear(v.intData());
    }

    public boolean isOccupied(Vertex v) {
        return bits.get(v.intData());
    }

    private static final Map<Graph<Vertex, DefaultEdge>, Occupation> occupationMap = new HashMap<>();
    public static Occupation getOccupation(Graph<Vertex, DefaultEdge> graph) {
        if (occupationMap.containsKey(graph)) {
            return occupationMap.get(graph);
        }
        checkData(graph);
        occupationMap.put(graph, new Occupation(graph.vertexSet().size()));
        return occupationMap.get(graph);
    }

    private static void checkData(Graph<Vertex, DefaultEdge> graph) {
        List<Integer> datas = graph.vertexSet().stream().map(Vertex::intData).collect(Collectors.toList());
        assert datas.size() == new HashSet<>(datas).size();
        assert Collections.min(datas) == 0;
        assert Collections.max(datas) == datas.size()-1;
    }

    public void release(Graph<Vertex, DefaultEdge> graph) {
        occupationMap.remove(graph);
    }
}
