package com.charrey.pruning.cached;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;
import com.charrey.util.datastructures.MultipleKeyMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Set;
import java.util.stream.Collectors;

public class MReachCachedZeroDomainPruner  extends MReachCachedPruner {
    public MReachCachedZeroDomainPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation, VertexMatching vertexMatching, int nReachLevel) {
        super(settings, sourceGraph, targetGraph, occupation, vertexMatching, nReachLevel);
    }

    public MReachCachedZeroDomainPruner(MReachCachedZeroDomainPruner copyOf) {
        super(copyOf);
    }

    @Override
    public boolean isUnfruitfulCached(int verticesPlaced) {
        return sourceGraph.vertexSet().stream().anyMatch(x -> getDomain(x).isEmpty());
    }

    @Override
    public Pruner copy() {
        return new MReachCachedZeroDomainPruner(this);
    }
}
