package com.charrey.pruning.cached;

import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.AbstractOccupation;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;

public class MReachCachedAllDifferentPruner extends MReachCachedPruner {
    public MReachCachedAllDifferentPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, AbstractOccupation occupation, VertexMatching vertexMatching, int nReachLevel) {
        super(settings, sourceGraph, targetGraph, occupation, vertexMatching, nReachLevel);
    }

    @Override
    public boolean isUnfruitfulCached(int verticesPlaced) {
        return true;
    }

    @Override
    public Pruner copy() {
        MReachCachedAllDifferentPruner res = new MReachCachedAllDifferentPruner(settings, sourceGraph, targetGraph, occupation, vertexMatching, nReachLevel);
        return res;
    }
}
