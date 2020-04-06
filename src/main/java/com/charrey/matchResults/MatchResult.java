package com.charrey.matchResults;

import java.util.Set;

public abstract class MatchResult<V1, V2> {

    private final V1 from;
    private final V2 to;

    public MatchResult(V1 from, V2 to) {
        this.from = from;
        this.to = to;
    }

    public V1 getFrom() {
        return from;
    }

    public V2 getTo() {
        return to;
    }
}
