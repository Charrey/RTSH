package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;

public class ReachabilityFiltering extends FilteringSettings {

    private final FilteringSettings innerFilter = new UnmatchedDegreesFiltering();

    @Override
    public int serialized() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        return getClass().equals(o.getClass());
    }

    @Override
    public boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, GlobalOccupation occupation) {
        if (!innerFilter.filter(sourceGraph, targetGraph, sourceGraphVertex, targetGraphVertex, occupation)) {
            return false;
        }
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
