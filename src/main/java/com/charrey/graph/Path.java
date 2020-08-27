package com.charrey.graph;

import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import com.charrey.util.datastructures.TroveIterator;
import com.google.common.collect.Ordering;
import gnu.trove.TCollections;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.procedure.TIntProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * The type Path saves a path of a MyGraph.
 */
public class Path implements Comparable<Path>, Iterable<Integer> {

    @NotNull
    private final TIntLinkedList vertexList;
    @NotNull
    private final Set<Integer> containing;
    private final int initialVertex;
    private final Graph<Integer, MyEdge> graph;


    /**
     * Instantiates a new Path, and immediately adds an initial vertex.
     *
     * @param graph         the graph of which vertices may be added to this Path.
     * @param initialVertex the initial vertex
     */
    public Path(@NotNull Graph<Integer, MyEdge> graph, int initialVertex) {
        this.initialVertex = initialVertex;
        vertexList = new TIntLinkedList();
        containing = new HashSet<>();
        this.graph = graph;
        append(initialVertex);
    }

    /**
     * Instantiates a new Path from the JGraphT implementation of Path that is semantically equivalent.
     *
     * @param graph the graph of which vertices may be added to this Path.
     * @param gPath the JGraphT path to convert to our own datatype.
     */
    public Path(@NotNull Graph<Integer, MyEdge> graph, @NotNull GraphPath<Integer, MyEdge> gPath) {
        this.vertexList = new TIntLinkedList();
        this.containing = new HashSet<>();
        this.initialVertex = gPath.getStartVertex();
        List<Integer> otherVertexList = gPath.getVertexList();
        this.graph = graph;
        for (int vertex : otherVertexList) {
            append(vertex);
        }
    }

    /**
     * Creates a path from a list of vertices, reading it left to right.
     *
     * @param graph      the graph of which vertices may be added to this Path.
     * @param vertexList the list of vertices to create a graph path from.
     */
    public Path(@NotNull Graph<Integer, MyEdge> graph, @NotNull List<Integer> vertexList) {
        this.graph = graph;
        this.vertexList = new TIntLinkedList();
        this.containing = new HashSet<>();
        this.initialVertex = vertexList.get(0);
        vertexList.forEach(this::append);
    }

    /**
     * Creates a path from a list of vertices, reading it left to right.
     *
     * @param graph      the graph of which vertices may be added to this Path.
     * @param vertexList the list of vertices to create a graph path from.
     */
    public Path(@NotNull Graph<Integer, MyEdge> graph, @NotNull TIntList vertexList) {
        this.graph = graph;
        this.vertexList = new TIntLinkedList();
        this.containing = new HashSet<>();
        this.initialVertex = vertexList.get(0);
        vertexList.forEach(i -> {
            append(i);
            return true;
        });
    }

    /**
     * Copies a path. This is a shallow copy, i.e. the vertex objects are the same as in the path this was copied from.
     *
     * @param copyOf the path to copy from.
     */
    public Path(@NotNull Path copyOf) {
        this.vertexList = new TIntLinkedList(copyOf.vertexList);
        this.containing = new HashSet<>(copyOf.containing);
        this.initialVertex = copyOf.initialVertex;
        this.graph = copyOf.graph;
    }

    /**
     * Returns this path as a list of Vertices. The order is such that traversal of the path is equivalent to traversal of this list from left to right.
     *
     * @return a list view of this path.
     */
    @NotNull
    public TIntList asList() {
        return TCollections.unmodifiableList(vertexList);
    }


    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return new TroveIterator(vertexList.iterator());
    }

    /**
     * Performs an action for each vertex in this path.
     *
     * @param consumer the consumer
     */
    public void forEach(Consumer<? super Integer> consumer) {
        vertexList.forEach(i -> {
            consumer.accept(i);
            return true;
        });
    }

    public double getWeight() {
        double total = 0d;
        for (int i = 0; i < vertexList.size() - 1; i++) {
            total += graph.getEdgeWeight(graph.getEdge(vertexList.get(i), vertexList.get(i + 1)));
        }
        return total;
    }

    /**
     * Appends a vertex to the end of this path. Throws IllegalStateException if the vertex is already in this path or if the graph has no such connection.
     *
     * @param toAdd vertex to append to this Path.
     */
    public void append(Integer toAdd) {
        if (!containing.contains(toAdd) ||  vertexList.get(0) == toAdd) {
            if (!containing.isEmpty() && graph.getEdge(vertexList.get(vertexList.size() - 1), toAdd) == null) {
                throw new IllegalStateException("Attempt to add a vertex " + toAdd + " on this path (" + vertexList  + ") that is not connected to the current head. Graph:\n" + graph);
            }
            containing.add(toAdd);
            vertexList.add(toAdd);
        } else {
            throw new IllegalStateException("Vertex already in this path.");
        }
    }

    /**
     * Returns the last vertex in this path.
     *
     * @return the last vertex
     */
    public int last() {
        return vertexList.get(vertexList.size() - 1);
    }


    /**
     * Returns the length (in vertices) of this path.
     *
     * @return the length of this path
     */
    public int length() {
        return vertexList.size();
    }

    /**
     * Returns whether this path is empty (i.e. does not contain vertices).
     *
     * @return whether the path is empty
     */
    public boolean isEmpty() {
        return vertexList.isEmpty();
    }

    /**
     * Removes the last vertex in this path. Throws IndexOutOfBoundsException if the Path is empty.
     *
     * @return the vertex that was removed.
     */
    public int removeLast() {
        int removed = vertexList.removeAt(vertexList.size() - 1);
        if (!vertexList.contains(removed)) {
            containing.remove(removed);
        }
        return removed;
    }

    /**
     * Returns whether this path contains some vertex.
     *
     * @param vertex the vertex to check
     * @return whether the path contains that vertex
     */
    public boolean contains(Integer vertex) {
        return containing.contains(vertex);
    }

    /**
     * Returns a vertex in a specific position of this path by index, where index=0 is the first vertex and index=length() - 1 is the last vertex.
     *
     * @param i the index of the vertex to return
     * @return the vertex at that index
     */
    public int get(int i) {
        return vertexList.get(i);
    }

    /**
     * Returns a list view of the vertices in this path EXCEPT the first and last vertex.
     *
     * @return a list of all intermediate vertices
     */
    @NotNull
    public Path intermediate() {
        return new Path(graph, vertexList.subList(1, vertexList.size() - 1));
    }

    /**
     * Returns the first vertex in this path
     *
     * @return the first vertex
     */
    public int first() {
        return vertexList.get(0);
    }

    @NotNull
    @Override
    public String toString() {
        return "Path{" + vertexList + '}';
    }


    @Override
    public boolean equals(@Nullable Object o) {
       return this == o;
    }

    public boolean isEqualTo(Path other) {
        return vertexList.equals(other.vertexList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertexList);
    }

    @Override
    public int compareTo(@NotNull Path o) {
        for (int i = 0; i < Math.min(length(), o.length()); i++) {
            int res1 = get(i);
            int res2 = o.get(i);
            if (res1 != res2) {
                return Integer.compare(res1, res2);
            }
        }
        return Integer.compare(length(), o.length());
    }

    public void insert(int index, int value) {
        if (!containing.contains(value)) {
            if (index == length()) {
                append(value);
            } else {
                if (index > 0 && graph.getEdge(vertexList.get(index - 1), value) == null) {
                    throw new IllegalStateException("Attempt to insert a vertex " + value + " that is not connected to its predecessor " + vertexList.get(index - 1) + " in graph:\n" + graph);
                }
                if (graph.getEdge(value, vertexList.get(index)) == null) {
                    throw new IllegalStateException("Attempt to insert a vertex " + value + " that is not connected to its successor " + vertexList.get(index) + " in graph:\n" + graph);
                }
                containing.add(value);
                vertexList.insert(index, value);
            }
        } else {
            throw new IllegalStateException("Vertex already in this path.");
        }
    }

    public void insertUnsafe(int index, int value) {
        if (index == length()) {
            append(value);
        } else {
            containing.add(value);
            vertexList.insert(index, value);
        }
    }

    public boolean noneMatch(Predicate<Integer> predicate) {
        final boolean[] toReturn = {true};
        this.vertexList.forEach(i -> {
            if (predicate.test(i)) {
                toReturn[0] = false;
                return false;
            } else {
                return true;
            }
        });
        return toReturn[0];
    }

    public Path subPath(int from, int to) {
        return new Path(graph, vertexList.subList(from, to));
    }

    public IntStream stream() {
        return Arrays.stream(vertexList.toArray());
    }



    public Graph<Integer, MyEdge> getGraph() {
        return graph;
    }


}
