package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;

public abstract class GraphVertexMapper {

    public abstract Mapping apply(@NotNull MyGraph graph);

}
