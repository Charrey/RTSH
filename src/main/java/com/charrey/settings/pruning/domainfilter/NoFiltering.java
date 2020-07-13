package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;

public class NoFiltering extends FilteringSettings {
    @Override
    public int serialized() {
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        return getClass().equals(o.getClass());
    }

    @Override
    public boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, GlobalOccupation occupation) {
        return true;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
