package com.charrey;

import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class Occupation {

    private final BitSet routingBits;
    private final BitSet vertexBits;

    private Occupation(int size){
        this.routingBits = new BitSet(size);
        this.vertexBits = new BitSet(size);
    }

    public void occupyRouting(Vertex v) {
        assert !routingBits.get(v.intData());
        routingBits.set(v.intData());
    }

    public void occupyVertex(Vertex v) {
        assert !routingBits.get(v.intData());
        assert !vertexBits.get(v.intData());
        vertexBits.set(v.intData());
    }

    public void releaseRouting(Vertex v) {
        assert routingBits.get(v.intData());
        routingBits.clear(v.intData());
    }

    public void releaseVertex(Vertex v) {
        assert vertexBits.get(v.intData());
        vertexBits.clear(v.intData());
    }

    public boolean isOccupiedRouting(Vertex v) {
        return routingBits.get(v.intData());
    }

    private boolean isOccupiedVertex(Vertex v) {
        return vertexBits.get(v.intData());
    }

    public boolean isOccupied(Vertex v) {
        return isOccupiedRouting(v) || isOccupiedVertex(v);
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
