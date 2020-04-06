package com.charrey.matchResults;

import com.charrey.State;

import java.util.Collections;
import java.util.Set;

public class SuccessMatchResult<V1 extends Comparable<V1>, V2 extends Comparable<V2>> extends MatchResult<V1, V2> {

    private final State<V2> newState;

    public SuccessMatchResult(V1 from, V2 to, State<V2> newState) {
        super(from, to);
        this.newState = newState;
    }

    public State<V2> getState() {
        return newState;
    }


}
