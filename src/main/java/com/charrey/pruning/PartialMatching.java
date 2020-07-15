package com.charrey.pruning;

import com.charrey.graph.Path;
import gnu.trove.TCollections;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Set;

public class PartialMatching {

    private final TIntList vertexMapping;
    private final TIntObjectMap<Set<Path>> edgeMapping;
    private final TIntSet partialPath;

    public PartialMatching() {
        this(new TIntArrayList());
    }

    public PartialMatching(TIntList vertexMapping) {
        this(vertexMapping, new TIntObjectHashMap<>());
    }

    public PartialMatching(TIntList vertexMapping, TIntObjectMap<Set<Path>> edgeMapping) {
        this(vertexMapping, edgeMapping, new TIntHashSet());
    }

    public PartialMatching(TIntList vertexMapping, TIntObjectMap<Set<Path>> edgeMapping, TIntSet partialPath) {
        this.vertexMapping = TCollections.unmodifiableList(vertexMapping);
        this.edgeMapping = TCollections.unmodifiableMap(edgeMapping);
        this.partialPath = TCollections.unmodifiableSet(partialPath);
    }

    public TIntList getVertexMapping() {
        return vertexMapping;
    }

    public TIntObjectMap<Set<Path>> getEdgeMapping() {
        return edgeMapping;
    }

    public TIntSet getPartialPath() {
        return partialPath;
    }
}
