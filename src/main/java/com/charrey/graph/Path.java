package com.charrey.graph;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;

public class Path {

    private final ArrayList<Vertex> path;
    private final BitSet containing;
    private final Vertex initialVertex;

    public Path(Vertex initialVertex, int maxSize) {
        this.initialVertex = initialVertex;
        path = new ArrayList<>(maxSize);
        containing = new BitSet(maxSize);
        append(initialVertex);
    }

    public void forEach(Consumer<? super Vertex> consumer) {
        path.forEach(consumer);
    }

    public Path(GraphPath<Vertex, DefaultEdge> gPath) {
        this.path = new ArrayList<>(gPath.getLength());
        this.containing = new BitSet(gPath.getGraph().vertexSet().size());
        this.initialVertex = gPath.getStartVertex();
        for (int i = 0; i <= gPath.getLength(); i++) {
            append(gPath.getVertexList().get(i));
        }
    }

    public void reinit() {
        containing.clear();
        path.clear();
        path.add(initialVertex);
    }

    public Path(Path found) {
        this.path = new ArrayList<>(found.path);
        this.containing = (BitSet) found.containing.clone();
        this.initialVertex = found.initialVertex;
    }

    public void append(Vertex toAdd) {
        if (!containing.get(toAdd.data())) {
            containing.set(toAdd.data());
            path.add(toAdd);
        }
    }

    public Vertex head() {
        return path.get(path.size()-1);
    }


    public int length() {
        return path.size();
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public Vertex removeHead() {
        Vertex removed = path.remove(path.size()-1);
        containing.clear(removed.data());
        return removed;
    }

    public boolean contains(Vertex vertex) {
        return containing.get(vertex.data());
    }

    public Vertex get(int i) {
        return path.get(i);
    }

    public List<Vertex> intermediate() {
        return path.subList(1, path.size() - 1);
    }

    public Vertex tail() {
        return path.get(0);
    }

    @Override
    public String toString() {
        return "Path{" + path + '}';
    }


}
