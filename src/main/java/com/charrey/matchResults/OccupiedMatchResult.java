package com.charrey.matchResults;

import com.charrey.State;

import java.util.Collections;
import java.util.Set;

public class OccupiedMatchResult<V1 extends Comparable<V1>, V2 extends Comparable<V2>> extends MatchResult<V1, V2> {


    public OccupiedMatchResult(V1 from, V2 to) {
        super(from, to);
    }

}
