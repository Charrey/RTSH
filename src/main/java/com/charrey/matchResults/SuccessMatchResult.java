package com.charrey.matchResults;

import com.charrey.State;
import com.charrey.graph.AttributedVertex;

import java.util.Collections;
import java.util.Set;

public class SuccessMatchResult extends MatchResult {

    private final State newState;

    public SuccessMatchResult(AttributedVertex from, AttributedVertex to, State newState) {
        super(from, to);
        this.newState = newState;
    }

    public State getState() {
        return newState;
    }


}
