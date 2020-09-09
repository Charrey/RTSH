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
        super(copyOf.settings, copyOf.sourceGraph, copyOf.targetGraph, copyOf.occupation, copyOf.vertexMatching, copyOf.nReachLevel);
        domain.clear();
        for (TIntObjectMap<TIntSet> map : copyOf.domain) {
            TIntObjectMap<TIntSet> toAdd = new TIntObjectHashMap<>();
            map.forEachEntry((a, b) -> {
                toAdd.put(a, new TIntHashSet(b));
                return true;
            });
            domain.add(toAdd);
        }
        reverseDomain.clear();
        for (TIntObjectMap<TIntList> map : copyOf.reverseDomain) {
            TIntObjectMap<TIntList> toAdd = new TIntObjectHashMap<>();
            map.forEachEntry((a, b) -> {
                toAdd.put(a, new TIntLinkedList(b));
                return true;
            });
            reverseDomain.add(toAdd);
        }
        for (MultipleKeyMap<Path>.Entry entry : copyOf.reachabilityCache.entrySet()) {
            reachabilityCache.put(entry.getFirstKey(), entry.getSecondKey(), new Path(entry.getValue()));
        }
        copyOf.reversePathLookup.forEachEntry((key, value) -> {
            reversePathLookup.put(key, value.stream().map(Path::new).collect(Collectors.toSet()));
            return true;
        });
    }

    @Override
    public boolean isUnfruitfulCached(int verticesPlaced) {
        //Set<Integer> emptyDomains = sourceGraph.vertexSet().stream().filter(x -> getDomain(x).isEmpty()).collect(Collectors.toSet());
        return sourceGraph.vertexSet().stream().anyMatch(x -> getDomain(x).isEmpty());
    }

    @Override
    public Pruner copy() {
        return new MReachCachedZeroDomainPruner(this);
    }
}
