package com.charrey.pruning.serial;

import com.charrey.graph.Path;
import com.charrey.util.Util;
import gnu.trove.TCollections;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PartialMatching {

    private final List<Integer> vertexMapping;
    private final TIntObjectMap<Set<Path>> edgeMapping;
    private final TIntSet partialPath;

    public PartialMatching() {
        this(Collections.synchronizedList(new ArrayList<>()));
    }

    public PartialMatching(List<Integer> vertexMapping) {
        this(vertexMapping, new TIntObjectHashMap<>(), Util.emptyTIntSet);
    }

//    public PartialMatching(TIntList vertexMapping, TIntObjectMap<Set<Path>> edgeMapping) {
//        this(vertexMapping, edgeMapping, new TIntHashSet());
//    }

    public PartialMatching(List<Integer> vertexMapping, TIntObjectMap<Set<Path>> edgeMapping, TIntSet partialPath) {
        this.vertexMapping = Collections.unmodifiableList(vertexMapping);
        this.edgeMapping = TCollections.unmodifiableMap(edgeMapping);
        this.partialPath = TCollections.unmodifiableSet(partialPath);
    }

    public List<Integer> getVertexMapping() {
        return vertexMapping;
    }

    public TIntObjectMap<Set<Path>> getEdgeMapping() {
        return edgeMapping;
    }

    public TIntSet getPartialPath() {
        return partialPath;
    }
}
