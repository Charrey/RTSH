package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import gnu.trove.set.TIntSet;

public class NReachabilityFiltering extends FilteringSettings {

    private final FilteringSettings innerFilter = new UnmatchedDegreesFiltering();

    @Override
    public boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, GlobalOccupation occupation) {
        if (!innerFilter.filter(sourceGraph, targetGraph, sourceGraphVertex, targetGraphVertex, occupation)) {
            return false;
        }
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public TIntSet sourceVerticestoReCheck(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, GlobalOccupation occupation) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public TIntSet targetVerticestoReCheck(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, GlobalOccupation occupation) {
        throw new UnsupportedOperationException(); //TODO
    }
}
