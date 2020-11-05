package com.charrey.settings.pruning.domainfilter;

import com.charrey.graph.MyGraph;

public class LabelDegreeFiltering implements FilteringSettings {



    public boolean filter(MyGraph sourceGraph, MyGraph targetGraph, int sourceGraphVertex, int targetGraphVertex) {
        return sourceGraph.getLabels(sourceGraphVertex).containsAll(targetGraph.getLabels(targetGraphVertex)) &&
                targetGraph.inDegreeOf(targetGraphVertex) >= sourceGraph.inDegreeOf(sourceGraphVertex) &&
                targetGraph.outDegreeOf(targetGraphVertex) >= sourceGraph.outDegreeOf(sourceGraphVertex);
    }


    @Override
    public FilteringSettings newInstance() {
        return new LabelDegreeFiltering();
    }

}
