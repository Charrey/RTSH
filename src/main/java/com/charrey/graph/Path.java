package com.charrey.graph;

import com.charrey.graph.generation.MyGraph;
import com.google.common.collect.Ordering;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Path saves a path of a MyGraph.
 */
public class Path implements Comparable<Path> {

    @NotNull
    private final List<Vertex> path;
    @NotNull
    private final Set<Integer> containing;
    private final Vertex initialVertex;
    private final MyGraph graph;

    /**
     * Instantiates a new Path, and immediately adds an initial vertex.
     *
     * @param graph         the graph of which vertices may be added to this Path.
     * @param initialVertex the initial vertex
     */
    public Path(@NotNull MyGraph graph, @NotNull Vertex initialVertex) {
        this.initialVertex = initialVertex;
        path = new LinkedList<>();
        containing = new HashSet<>();
        this.graph = graph;
        append(initialVertex);
    }

    /**
     * Returns this path as a list of Vertices. The order is such that traversal of the path is equivalent to traversal of this list from left to right.
     *
     * @return a list view of this path.
     */
    @NotNull
    public List<Vertex> asList() {
        return Collections.unmodifiableList(path);
    }

    /**
     * Provides a stream of vertices in this path from the initial vertex to the final vertex.
     *
     * @return a stream of vertices in this path.
     */
    public Stream<Vertex> stream() {
        return path.stream();
    }

    /**
     * Performs an action for each vertex in this path.
     *
     * @param consumer the consumer
     */
    public void forEach(Consumer<? super Vertex> consumer) {
        path.forEach(consumer);
    }

    /**
     * Instantiates a new Path from the JGraphT implementation of Path that is semantically equivalent.
     *
     * @param graph the graph of which vertices may be added to this Path.
     * @param gPath the JGraphT path to convert to our own datatype.
     */
    public Path(@NotNull MyGraph graph, @NotNull GraphPath<Vertex, DefaultEdge> gPath) {
        this.path = new LinkedList<>();
        this.containing = new HashSet<>();
        this.initialVertex = gPath.getStartVertex();
        List<Vertex> vertexList = gPath.getVertexList();
        this.graph = graph;
        for (Vertex vertex : vertexList) {
            append(vertex);
        }
    }

    /**
     * Creates a path from a list of vertices, reading it left to right.
     *
     * @param graph the graph of which vertices may be added to this Path.
     * @param path  the list of vertices to create a graph path from.
     */
    public Path(@NotNull MyGraph graph, @NotNull List<Vertex> path) {
        this.graph = graph;
        this.path = new LinkedList<>();
        this.containing = new HashSet<>();
        this.initialVertex = path.get(0);
        path.forEach(this::append);
    }

    /**
     * Copies a path. This is a shallow copy, i.e. the vertex objects are the same as in the path this was copied from.
     *
     * @param copyOf the path to copy from.
     */
    public Path(@NotNull Path copyOf) {
        this.path = new LinkedList<>(copyOf.path);
        this.containing = new HashSet<>(copyOf.containing);
        this.initialVertex = copyOf.initialVertex;
        this.graph = copyOf.graph;
    }

    /**
     * Appends a vertex to the end of this path. Throws IllegalStateException if the vertex is already in this path or if the graph has no such connection.
     *
     * @param toAdd vertex to append to this Path.
     */
    public void append(@NotNull Vertex toAdd) {
        if (!containing.contains(toAdd.data())) {
            if (!containing.isEmpty() && graph.getEdge(path.get(path.size() - 1), toAdd) == null) {
                throw new IllegalStateException("Attempt to add a vertex on this path that is not connected to the current head.");
            }
            containing.add(toAdd.data());
            path.add(toAdd);
        } else {
            throw new IllegalStateException("Vertex already in this path.");
        }
    }

    /**
     * Returns the last vertex in this path.
     *
     * @return the last vertex
     */
    public Vertex last() {
        return path.get(path.size() - 1);
    }


    /**
     * Returns the length (in vertices) of this path.
     * @return the length of this path
     */
    public int length() {
        return path.size();
    }

    /**
     * Returns whether this path is empty (i.e. does not contain vertices).
     *
     * @return whether the path is empty
     */
    public boolean isEmpty() {
        return path.isEmpty();
    }

    /**
     * Removes the last vertex in this path. Throws IndexOutOfBoundsException if the Path is empty.
     * @return the vertex that was removed.
     */
    public Vertex removeLast() {
        Vertex removed = path.remove(path.size() - 1);
        containing.remove(removed.data());
        return removed;
    }

    /**
     * Returns whether this path contains some vertex.
     *
     * @param vertex the vertex to check
     * @return whether the path contains that vertex
     */
    public boolean contains(@NotNull Vertex vertex) {
        return containing.contains(vertex.data());
    }

    /**
     * Returns a vertex in a specific position of this path by index, where index=0 is the first vertex and index=length() - 1 is the last vertex.
     *
     * @param i the index of the vertex to return
     * @return the vertex at that index
     */
    public Vertex get(int i) {
        return path.get(i);
    }

    /**
     * Returns a list view of the vertices in this path EXCEPT the first and last vertex.
     *
     * @return a list of all intermediate vertices
     */
    @NotNull
    public List<Vertex> intermediate() {
        return path.subList(1, path.size() - 1);
    }

    /**
     * Returns the first vertex in this path
     *
     * @return the first vertex
     */
    public Vertex first() {
        return path.get(0);
    }

    @NotNull
    @Override
    public String toString() {
        return "Path{" + path + '}';
    }


    @Override
    public boolean equals(@Nullable Object o) {
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
