package com.charrey.graph;

import com.google.common.collect.Ordering;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Path implements Comparable<Path> {

    private final List<Vertex> path;
    private final BitSet containing;
    private final Vertex initialVertex;

    public Path(Vertex initialVertex, int maxSize) {
        this.initialVertex = initialVertex;
        path = new ArrayList<>(maxSize);
        containing = new BitSet(maxSize);
        append(initialVertex);
    }

    public List<Vertex> asList() {
        return Collections.unmodifiableList(path);
    }

    public Stream<Vertex> stream() {
        return path.stream();
    }

    public void forEach(Consumer<? super Vertex> consumer) {
        path.forEach(consumer);
    }

    public Path(GraphPath<Vertex, DefaultEdge> gPath) {
        this.path = new LinkedList<>();
        this.containing = new BitSet(gPath.getGraph().vertexSet().size());
        this.initialVertex = gPath.getStartVertex();
        List<Vertex> vertexList = gPath.getVertexList();
        for (Vertex vertex : vertexList) {
            append(vertex);
        }
    }

    public Path(List<Vertex> path) {
        this.path = new LinkedList<>();
        this.containing = new BitSet();
        this.initialVertex = path.get(0);
        path.forEach(this::append);
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
        } else {
            assert false;
            throw new IllegalStateException("Vertex already in this path.");
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path1 = (Path) o;
        return path.equals(path1.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public int compareTo(@NotNull Path o) {
        return Ordering.natural().lexicographical().compare(this.path.stream().mapToInt(Vertex::data).boxed().collect(Collectors.toList()), o.path.stream().mapToInt(Vertex::data).boxed().collect(Collectors.toList()));
    }
}
