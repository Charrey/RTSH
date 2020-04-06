package com.charrey.matchResults;

import com.charrey.graph.AttributedVertex;
import com.charrey.graph.Path;

import java.util.Set;

public class SuccessWithExtraVertices extends MatchResult {

    public SuccessWithExtraVertices(AttributedVertex from, AttributedVertex to) {
        super(from, to);
    }

    public Set<Set<AttributedVertex>> getContested() {
        throw new UnsupportedOperationException();
    }

    public AttributedVertex getPlacedVertex() {
        throw new UnsupportedOperationException();
    }

    public Set<Path<AttributedVertex>> getAddedPaths() {
        throw new UnsupportedOperationException();
    }

    public boolean hasWorsePaths() {
        throw new UnsupportedOperationException();
    }
}
