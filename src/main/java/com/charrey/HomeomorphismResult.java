package com.charrey;

import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import org.jetbrains.annotations.NotNull;

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

    static final HomeomorphismResult COMPATIBILITY_FAIL = new HomeomorphismResult(true, null, null, 0);

    @NotNull
    static HomeomorphismResult ofFailed(long iterations) {
        return new HomeomorphismResult(true, null, null, iterations);
    }

    @NotNull
    static HomeomorphismResult ofSucceed(VertexMatching vertexMatching, EdgeMatching edgeMatching, long iterations) {
        return new HomeomorphismResult(false, vertexMatching, edgeMatching, iterations);
    }

    @NotNull
    public String toString() {
        return matching.toString() + "\n" + edgeMatching.toString();
    }

}
