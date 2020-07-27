package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;

public interface GraphVertexMapper {

    Mapping apply(@NotNull MyGraph graph);

}
