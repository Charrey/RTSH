package com.charrey.router;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;

public class ControlPointInteratorState {

    public int controlPointCount;
    public int targetVertexToTryNext;
    public Vertex currentTarget;
    public Path tried;

    public ControlPointInteratorState(Vertex currentTarget, int controlPointCount, int targetVertexToTryNext) {
        this.currentTarget = currentTarget;
        this.targetVertexToTryNext = targetVertexToTryNext;
        this.controlPointCount = controlPointCount;
    }
}
