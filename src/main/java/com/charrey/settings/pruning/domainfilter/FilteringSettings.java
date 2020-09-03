package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import gnu.trove.set.TIntSet;

public interface FilteringSettings {

    boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, GlobalOccupation occupation, VertexMatching vmatching);

    TIntSet sourceVerticestoReCheck(MyGraph sourceGraph, MyGraph targetGraph, int targetGraphVertex, GlobalOccupation occupation);

    TIntSet targetVerticestoReCheck(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, GlobalOccupation occupation);
}
