package com.charrey.util;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.AbstractOccupation;
import gnu.trove.TCollections;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.graph.MaskSubgraph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility methods
 */
public class Util {

    public static final TIntSet emptyTIntSet = TCollections.unmodifiableSet(new TIntHashSet());

    private Util() {
    }

    /**
     * Selects a random element from a collection that fulfills a specific predicate.
     *
     * @param <V>        the type of elements in the collection
     * @param collection the collection of elements
     * @param eligable   predicate that determines whether an element is eligable to be returned.
     * @param random     randomgenerator for deterministic randomness
     * @return a random element from the collection
     */
    public static <V> V selectRandom(@NotNull Collection<V> collection, Predicate<V> eligable, RandomGenerator random) {
        List<V> myList = collection.stream().filter(eligable).collect(Collectors.toList());
        return myList.get(random.nextInt(myList.size()));
    }

    /**
     * Shuffles an integer array
     *
     * @param array the integer to be shuffled
     */
    public static void shuffle(int[] array, RandomGenerator gen) {
        for (int i = 0; i < array.length; i++) {
            int randomPosition = gen.nextInt(array.length);
            int temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }
    }


    /**
     * Finds the shortest path between two vertices that avoids already placed vertices.
     *
     * @param targetGraph       the target graph
     * @param globalOccupation  the occupation where intermediate nodes are registered
     * @param localOccupation   the local occupation of vertices matched in this very path
     * @param from              the source vertex
     * @param to                the target vertex
     * @param refuseLongerPaths whether to refuse paths that use unnecessarily many resources
     * @param tail              the final goal vertex of this path (if this is a recursive call)
     * @param allowedToBeOccupied set of vertices that are allowed to be occupied by the global occupation in this path.
     * @return the path
     */
    @NotNull
    public static Optional<Path> filteredShortestPath(@NotNull MyGraph targetGraph, @NotNull AbstractOccupation globalOccupation, @NotNull TIntSet localOccupation, int from, int to, boolean refuseLongerPaths, int tail, TIntSet allowedToBeOccupied) {
        assert targetGraph.containsVertex(from);
        assert targetGraph.containsVertex(to);
        Graph<Integer, MyEdge> fakeGraph = new MaskSubgraph<>(targetGraph, x ->
                x != from &&
                        x != to &&
                        (localOccupation.contains(x) || (globalOccupation.isOccupied(x) && !allowedToBeOccupied.contains(x)) ||
                                (refuseLongerPaths && violatesLongerPaths(targetGraph, x, from, to, tail, localOccupation))), x -> false);
        GraphPath<Integer, MyEdge> algo = new BFSShortestPath<>(fakeGraph).getPath(from, to);
        return algo == null ? Optional.empty() : Optional.of(new Path(targetGraph, algo));
    }

    public static Optional<Path> filteredShortestPath(@NotNull MyGraph targetGraph, @NotNull AbstractOccupation globalOccupation, @NotNull TIntSet localOccupation, TIntSet from, TIntSet to) {
        int virtualSource = targetGraph.addVertex();
        int virtualTarget = targetGraph.addVertex();
        to.forEach(integer -> {
            targetGraph.addEdge(integer, virtualTarget);
            return true;
        });
        from.forEach(integer -> {
            targetGraph.addEdge(virtualSource, integer);
            return true;
        });
        TIntSet allowedToBeVertexOccupied = new TIntHashSet();
        allowedToBeVertexOccupied.addAll(from);
        allowedToBeVertexOccupied.addAll(to);

        Optional<Path> toReturn = filteredShortestPath(targetGraph, globalOccupation, localOccupation, virtualSource, virtualTarget, false, -1, allowedToBeVertexOccupied);

        if (toReturn.isPresent()) {
            toReturn = Optional.of(toReturn.get().intermediate());
        }
        targetGraph.removeVertex(virtualSource);
        targetGraph.removeVertex(virtualTarget);
        return toReturn;
    }

    /**
     * Glues two paths together, where the end vertex of one path is the start vertex of another.
     *
     * @param graph the graph to which the vertices belong
     * @param left  the left path
     * @param right the right path
     * @return the glued path that starts with the left path's initial vertex and ends with the right path's final vertex.
     */
    @NotNull
    public static Path merge(@NotNull MyGraph graph, @NotNull Path left, @NotNull Path right) {
        Path toReturn = new Path(graph, left.first());
        for (int i = 1; i < left.length() - 1; i++) {
            toReturn.append(left.get(i));
        }
        for (int i = 0; i < right.length(); i++) {
            toReturn.append(right.get(i));
        }
        return toReturn;
    }

    private static boolean violatesLongerPaths(Graph<Integer, MyEdge> targetGraph, int allowableIntermediateVertex, int from, int to, int goal, TIntSet localOccupation) {
        if (goal != from && (targetGraph.getEdge(goal, allowableIntermediateVertex) != null)) {
            return true;
        }
        List<Integer> intermediateVertexSuccessors = Graphs.successorListOf(targetGraph, allowableIntermediateVertex);

        final boolean[] toReturnTrue = {false};
        localOccupation.forEach(y -> {
            if (y != to && intermediateVertexSuccessors.contains(y)) {
                toReturnTrue[0] = true;
                return false;
            }
            return true;
        });
        return toReturnTrue[0];
    }





    /**
     * Returns the starting position of the first occurrence of the specified
     * target list within the specified source list, or -1 if there is no
     * such occurrence.  More formally, returns the lowest index {@code i}
     * such that {@code source.subList(i, i+target.size()).equals(target)},
     * or -1 if there is no such index.  (Returns -1 if
     * {@code target.size() > source.size()})
     *
     * <p>This implementation uses the "brute force" technique of scanning
     * over the source list, looking for a match with the target at each
     * location in turn.
     *
     * @param source the list in which to search for the first occurrence
     *        of {@code target}.
     * @param target the list to search for as a subList of {@code source}.
     * @return the starting position of the first occurrence of the specified
     *         target list within the specified source list, or -1 if there
     *         is no such occurrence.
     * @since  1.4
     */
    public static int indexOfSubList(int[] source, int[] target) {
        int sourceSize = source.length;
        int targetSize = target.length;
        int maxCandidate = sourceSize - targetSize;
        nextCand:
        for (int candidate = 0; candidate <= maxCandidate; candidate++) {
            for (int i=0, j=candidate; i<targetSize; i++, j++) {
                if (target[i] != source[j]) {
                    continue nextCand;  // Element mismatch, try next cand
                }
            }
            return candidate;  // All elements of candidate matched target
        }
        return -1;  // No candidate matched the target
    }
}
