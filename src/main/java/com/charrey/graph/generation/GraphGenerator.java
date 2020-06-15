package com.charrey.graph.generation;

import com.charrey.graph.Vertex;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.function.Supplier;

class GraphGenerator {


    public static class VertexGenerator implements Serializable, Supplier<Vertex> {
        private int current = 0;


        @NotNull
        @Override
        public Vertex get() {
            return new Vertex(current++);
        }
    }

}
