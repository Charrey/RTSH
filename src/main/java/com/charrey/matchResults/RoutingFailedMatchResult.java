package com.charrey.matchResults;

import java.util.Set;

public class RoutingFailedMatchResult<V1, V2> extends MatchResult<V1, V2> {


    public RoutingFailedMatchResult(V1 from, V2 to) {
        super(from, to);
    }

}
