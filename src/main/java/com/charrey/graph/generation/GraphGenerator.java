package com.charrey.graph.generation;

import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.Serializable;
import java.util.function.Supplier;

public class GraphGenerator {


    public static MyGraph getTarget() {
        MyGraph pattern = new MyGraph(false);
        Vertex p1 = pattern.addVertex().addLabel("normal");
        Vertex p2 = pattern.addVertex().addLabel("normal");
        Vertex p3 = pattern.addVertex().addLabel("normal");
        Vertex p4 = pattern.addVertex().addLabel("normal");
        pattern.addEdge(p1, p2);
        pattern.addEdge(p1, p3);
        pattern.addEdge(p1, p4);
        pattern.addEdge(p2, p3);
        pattern.addEdge(p2, p4);
        pattern.addEdge(p3, p4);
        return pattern;
    }


    public static MyGraph getPattern() {
        MyGraph pattern = new MyGraph(false);
        Vertex p0 = pattern.addVertex().addLabel("normal");
        Vertex p1 = pattern.addVertex().addLabel("normal");
        Vertex p2 = pattern.addVertex().addLabel("normal");
        Vertex p3 = pattern.addVertex().addLabel("normal");
        Vertex p4 = pattern.addVertex().addLabel("normal");
        pattern.addEdge(p0, p1);
        pattern.addEdge(p1, p2);
        pattern.addEdge(p0, p2);
        pattern.addEdge(p3, p1);
        pattern.addEdge(p3, p4);
        return pattern;
    }


    public static class IntGenerator implements Serializable, Supplier<Vertex> {
        private int current = 0;


        @Override
        public Vertex get() {
            return new Vertex(current++);
        }
    }

    public static class BasicEdgeSupplier implements Serializable, Supplier<DefaultEdge> {
        @Override
        public DefaultEdge get() {
            return new DefaultEdge();
        }
    }
}
