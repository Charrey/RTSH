package com.charrey;

import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;

public class Homeomorphism {
    private final EdgeMatching edgeMatching;
    private final VertexMatching vertexMatching;

    public Homeomorphism(VertexMatching vertexMatching, EdgeMatching edgeMatching) {
        this.vertexMatching = vertexMatching;
        this.edgeMatching = edgeMatching;
    }

    @Override
    public String toString() {
        String sb = "Homeomorphism{edgeMatching=" + edgeMatching +
                ", vertexMatching=" + vertexMatching +
                '}';
        return sb;
    }
}
