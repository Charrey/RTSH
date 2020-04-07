package com.charrey.matchResults;

import com.charrey.graph.Vertex;
import com.charrey.graph.Path;

import java.util.Set;

public class SuccessWithExtraVertices extends MatchResult {

    public SuccessWithExtraVertices(Vertex from, Vertex to) {
        super(from, to);
    }

    public Set<Set<Vertex>> getContested() {
        throw new UnsupportedOperationException();
    }

    public Vertex getPlacedVertex() {
        throw new UnsupportedOperationException();
    }

    public Set<Path<Vertex>> getAddedPaths() {
        throw new UnsupportedOperationException();
    }

    public boolean hasWorsePaths() {
        throw new UnsupportedOperationException();
    }
}
