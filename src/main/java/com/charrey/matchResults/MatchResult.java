package com.charrey.matchResults;

import com.charrey.graph.AttributedVertex;

import java.util.Set;

public abstract class MatchResult {

    private final AttributedVertex from;
    private final AttributedVertex to;

    public MatchResult(AttributedVertex from, AttributedVertex to) {
        this.from = from;
        this.to = to;
    }

    public AttributedVertex getFrom() {
        return from;
    }

    public AttributedVertex getTo() {
        return to;
    }
}
