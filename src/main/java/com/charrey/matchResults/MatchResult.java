package com.charrey.matchResults;

import com.charrey.graph.Vertex;

public abstract class MatchResult {

    private final Vertex from;
    private final Vertex to;

    public MatchResult(Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
    }

    public Vertex getFrom() {
        return from;
    }

    public Vertex getTo() {
        return to;
    }
}
