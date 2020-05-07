package com.charrey;

import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;

public class HomeomorphismResult {

    public final boolean failed;
    private final VertexMatching matching;
    private final EdgeMatching edgeMatching;
    public final long iterations;

    private HomeomorphismResult(boolean failed, VertexMatching matching, EdgeMatching edgeMatching, long iterations) {
        this.failed = failed;
        this.matching = matching;
        this.edgeMatching = edgeMatching;
        this.iterations = iterations;
    }

    public static final HomeomorphismResult COMPATIBILITY_FAIL = new HomeomorphismResult(true, null, null, 0);

    public static HomeomorphismResult ofFailed(long iterations) {
        return new HomeomorphismResult(true, null, null, iterations);
    }

    public static HomeomorphismResult ofSucceed(VertexMatching vertexMatching, EdgeMatching edgeMatching, long iterations) {
        return new HomeomorphismResult(false, vertexMatching, edgeMatching, iterations);
    }

}
