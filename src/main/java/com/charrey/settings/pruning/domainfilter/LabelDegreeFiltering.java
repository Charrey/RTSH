package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;

public class LabelDegreeFiltering extends FilteringSettings {

    @Override
    public int serialized() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return getClass().equals(o.getClass());
    }

    @Override
    public boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex, GlobalOccupation occupation) {
        return sourceGraph.getLabels(sourceGraphVertex).containsAll(targetGraph.getLabels(targetGraphVertex)) &&
                targetGraph.inDegreeOf(targetGraphVertex) >= sourceGraph.inDegreeOf(sourceGraphVertex) &&
                targetGraph.outDegreeOf(targetGraphVertex) >= sourceGraph.outDegreeOf(sourceGraphVertex);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
