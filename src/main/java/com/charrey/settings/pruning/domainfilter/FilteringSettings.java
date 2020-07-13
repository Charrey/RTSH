package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import org.jetbrains.annotations.NotNull;

public abstract class FilteringSettings implements Comparable<FilteringSettings> {


    public abstract int serialized();

    @Override
    public int compareTo(@NotNull FilteringSettings o) {
        return Integer.compare(serialized(), o.serialized());
    }

    @Override
    public abstract boolean equals(Object o);

    public abstract boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, GlobalOccupation occupation);
}
