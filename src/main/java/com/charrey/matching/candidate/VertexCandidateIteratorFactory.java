package com.charrey.matching.candidate;

import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;

public class VertexCandidateIteratorFactory {


    public static VertexCandidateIterator get(MyGraph sourceGraph, MyGraph targetGraph, Settings settings, GlobalOccupation occupation, int sourceGraphVertex, VertexMatching vertexMatching) {
        //return new IndexIterator(sourceGraph, targetGraph, sourceGraphVertex, settings, occupation);
        return new CloseFirstIterator(sourceGraph, targetGraph, settings, occupation, sourceGraphVertex, vertexMatching, 1);
    }
}
