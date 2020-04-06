package com.charrey.matchResults;

import com.charrey.graph.Path;

import java.util.Set;

public class SuccessWithExtraVertices<V1, V2> extends MatchResult<V1, V2> {

    public SuccessWithExtraVertices(V1 from, V2 to) {
        super(from, to);
    }

    public Set<Set<V2>> getContested() {
        throw new UnsupportedOperationException();
    }

    public V2 getPlacedVertex() {
        throw new UnsupportedOperationException();
    }

    public Set<Path<V2>> getAddedPaths() {
        throw new UnsupportedOperationException();
    }

    public boolean hasWorsePaths() {
        throw new UnsupportedOperationException();
    }
}
