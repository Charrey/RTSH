package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.AbstractOccupation;

public class NoFiltering implements FilteringSettings {

    @Override
    public boolean filter(MyGraph sourceGraph,
                          MyGraph targetGraph,
                          int sourceGraphVertex,
                          int targetGraphVertex,
                          AbstractOccupation occupation,
                          VertexMatching vertexMatching) {
        return true;
    }


    @Override
    public FilteringSettings newInstance() {
        return this;
    }


}
