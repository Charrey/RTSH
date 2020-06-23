package com.charrey.graph.generation;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class TestCase implements Serializable, Iterable<MyGraph> {
    public final MyGraph targetGraph;
    public final MyGraph sourceGraph;

    public TestCase(MyGraph sourceGraph, MyGraph targetGraph) {
        this.sourceGraph = sourceGraph;
        this.targetGraph = targetGraph;
    }

    @NotNull
    @Override
    public Iterator<MyGraph> iterator() {
       return List.of(sourceGraph, targetGraph).iterator();
    }
}
