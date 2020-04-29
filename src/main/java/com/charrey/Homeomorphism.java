package com.charrey;

import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;

public class Homeomorphism {
    private final EdgeMatching edgeMatching;
    private final VertexMatching vertexMatching;
    private final long iterations;

    public Homeomorphism(VertexMatching vertexMatching, EdgeMatching edgeMatching, long iterations) {
        this.vertexMatching = vertexMatching;
        this.edgeMatching = edgeMatching;
        this.iterations = iterations;
    }

    public long getIterations() {
        return iterations;
    }

    @Override
    public String toString() {
        return "Homeomorphism{edgeMatching=" + edgeMatching +
                ", vertexMatching=" + vertexMatching +
                '}';
    }
}
