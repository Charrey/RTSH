package com.charrey.pruning.cached;

import com.charrey.graph.MyGraph;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.ReadOnlyOccupation;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;

public class MReachCachedZeroDomainPruner  extends MReachCachedPruner {
    public MReachCachedZeroDomainPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, ReadOnlyOccupation occupation, VertexMatching vertexMatching, int nReachLevel) {
        super(settings, sourceGraph, targetGraph, occupation, vertexMatching, nReachLevel);
    }

    public MReachCachedZeroDomainPruner(MReachCachedZeroDomainPruner copyOf) {
        super(copyOf);
    }

    @Override
    public boolean isUnfruitful(int verticesPlaced, PartialMatchingProvider partialMatchingProvider, int lastPlaced) {
        return sourceGraph.vertexSet().stream().anyMatch(x -> getDomain(x).isEmpty());
    }

    @Override
    public Pruner copy() {
        return new MReachCachedZeroDomainPruner(this);
    }
}
