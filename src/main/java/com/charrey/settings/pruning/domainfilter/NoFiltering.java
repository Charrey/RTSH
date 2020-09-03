package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.util.Util;
import gnu.trove.set.TIntSet;

public class NoFiltering implements FilteringSettings {

    @Override
    public boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, GlobalOccupation occupation, VertexMatching vertexMatching) {
        return true;
    }

    @Override
    public TIntSet sourceVerticestoReCheck(MyGraph sourceGraph, MyGraph targetGraph, int targetGraphVertex, GlobalOccupation occupation) {
        return Util.emptyTIntSet;
    }

    @Override
    public TIntSet targetVerticestoReCheck(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, GlobalOccupation occupation) {
        return Util.emptyTIntSet;
    }


}
