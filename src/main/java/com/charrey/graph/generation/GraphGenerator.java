package com.charrey.graph.generation;

import com.charrey.graph.Vertex;

import java.io.Serializable;
import java.util.function.Supplier;

public class GraphGenerator {


    public static class VertexGenerator implements Serializable, Supplier<Vertex> {
        private int current = 0;


        @Override
        public Vertex get() {
            return new Vertex(current++);
        }
    }

}
