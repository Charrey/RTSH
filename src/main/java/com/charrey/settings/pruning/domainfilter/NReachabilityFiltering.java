package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.AbstractOccupation;

public class NReachabilityFiltering implements FilteringSettings {

    private final FilteringSettings innerFilter = new LabelDegreeFiltering();
    private final int level;

    public NReachabilityFiltering(int level) {
        this.level = level;
    }

    @Override
    public boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, AbstractOccupation occupation, VertexMatching vertexMatching) {
        if (!innerFilter.filter(sourceGraph, targetGraph, sourceGraphVertex, targetGraphVertex, occupation, vertexMatching)) {
            return false;
        }
        return true;
    }


    @Override
    public FilteringSettings newInstance() {
        return new NReachabilityFiltering(level);
    }

    public int getLevel() {
        return level;
    }
}
