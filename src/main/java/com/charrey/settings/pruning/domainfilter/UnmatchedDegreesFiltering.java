package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.AbstractOccupation;
import com.charrey.occupation.GlobalOccupation;
import org.jgrapht.Graphs;

public class UnmatchedDegreesFiltering implements FilteringSettings {

    @Override
    public boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, AbstractOccupation occupation, VertexMatching vertexMatching) {
        return occupation.isOccupied(targetGraphVertex) || (sourceGraph.getLabels(sourceGraphVertex).containsAll(targetGraph.getLabels(targetGraphVertex)) &&
                incomingUnmatched(targetGraph, targetGraphVertex, occupation) >= incomingGreater(sourceGraph, sourceGraphVertex) &&
                outgoingUnmatched(targetGraph, targetGraphVertex, occupation) >= outgoingGreater(sourceGraph ,sourceGraphVertex));
    }


    private int outgoingUnmatched(MyGraph targetGraph, int targetGraphVertex, AbstractOccupation occupation) {
        return Math.toIntExact(targetGraph.outgoingEdgesOf(targetGraphVertex)
                .stream()
                .map(x -> Graphs.getOppositeVertex(targetGraph, x, targetGraphVertex))
                .filter(x -> !occupation.isOccupied(x))
                .count());
    }

    private int incomingGreater(MyGraph sourceGraph, int vertex) {
        return Math.toIntExact(sourceGraph.incomingEdgesOf(vertex)
                .stream()
                .mapToInt(x -> Graphs.getOppositeVertex(sourceGraph, x, vertex))
                .filter(x -> x > vertex).count());
    }

    private int outgoingGreater(MyGraph sourceGraph, int vertex) {
        return Math.toIntExact(sourceGraph.outgoingEdgesOf(vertex)
                .stream()
                .mapToInt(x -> Graphs.getOppositeVertex(sourceGraph, x, vertex))
                .filter(x -> x > vertex).count());
    }

    private int incomingUnmatched(MyGraph targetGraph, int targetGraphVertex, AbstractOccupation occupation) {
        return Math.toIntExact(targetGraph.incomingEdgesOf(targetGraphVertex)
                .stream()
                .map(x -> Graphs.getOppositeVertex(targetGraph, x, targetGraphVertex))
                .filter(x -> !occupation.isOccupied(x))
                .count());
    }

    @Override
    public FilteringSettings newInstance() {
        return new UnmatchedDegreesFiltering();
    }


}
