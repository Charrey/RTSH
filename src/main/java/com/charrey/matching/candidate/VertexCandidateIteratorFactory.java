package com.charrey.matching.candidate;

import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;

public class VertexCandidateIteratorFactory {

    private VertexCandidateIteratorFactory() {
    }


    public static VertexCandidateIterator get(MyGraph sourceGraph, MyGraph targetGraph, Settings settings, GlobalOccupation occupation, int sourceGraphVertex, VertexMatching vertexMatching) {
        return switch (settings.getTargetVertexOrder()) {
            case LARGEST_DEGREE_FIRST -> new IndexIterator(sourceGraph, targetGraph, sourceGraphVertex, settings, occupation, vertexMatching);
            case CLOSEST_TO_MATCHED -> new CloseFirstIterator(sourceGraph, targetGraph, settings, occupation, sourceGraphVertex, vertexMatching);
        };
    }
}
