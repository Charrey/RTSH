package com.charrey.matchResults;

import com.charrey.State;
import com.charrey.graph.Vertex;

public class SuccessMatchResult extends MatchResult {

    private final State newState;

    public SuccessMatchResult(Vertex from, Vertex to, State newState) {
        super(from, to);
        this.newState = newState;
    }

    public State getState() {
        return newState;
    }


}
