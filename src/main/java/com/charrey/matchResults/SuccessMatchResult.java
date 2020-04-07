package com.charrey.matchResults;

import com.charrey.State;
import com.charrey.graph.Vertex;

public class SuccessMatchResult extends MatchResult {

    private final Vertex from;
    private final Vertex to;

    public SuccessMatchResult(Vertex from, Vertex to) {
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
