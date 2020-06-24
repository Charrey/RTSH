package com.charrey.algorithms;

import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.charrey.settings.PathIterationStrategy.DFS_ARBITRARY;
import static com.charrey.settings.PathIterationStrategy.DFS_GREEDY;

/**
 * Class that provides several properties of graphs that are often used and difficult to calculate.
 */
public class UtilityData {
    private final MyGraph targetGraph;
    private final MyGraph patternGraph;

    /**
     * Instantiates a new Utility data class. This needs to be done (instead of static calls) to allow for caching.
     *
     * @param sourceGraph the source graph
     * @param targetGraph  the target graph
     */
    public UtilityData(MyGraph sourceGraph, MyGraph targetGraph) {
        this.patternGraph = sourceGraph;
        this.targetGraph = targetGraph;
    }

//    private List<Integer> order;
//
//    /**
//     * Returns an appropriate source graph vertex order.
//     *
//     * @return the vertex order to follow in the matching process
//     */
//    public List<Integer> getOrder() {
//        if (order == null) {
//            order = new GreatestConstrainedFirst().apply(patternGraph);
//        }
//        return Collections.unmodifiableList(order);
//    }

    private Integer[][] compatibility;

    /**
     * Returns the compatibility of each source graph vertex to target graph vertices.
     *
     * @param neighbourHoodFiltering    whether to filter the domains of each vertex v such that all candidates have neighbourhoods that can emulate v's neighbourhood.
     * @param initialGlobalAllDifferent whether to apply AllDifferent to each possible matching to filter out candidates.
     * @return A 2d array compatibility such that for each source vertex with vertex ordering x, compatibility[x] is an array of suitable target graph candidates.
     */
    public Integer[][] getCompatibility(boolean neighbourHoodFiltering, boolean initialGlobalAllDifferent) {
        if (compatibility == null) {
            compatibility = new Integer[patternGraph.vertexSet().size()][];
            Map<Integer, Set<Integer>> inbetween = new CompatibilityChecker().get(patternGraph, targetGraph, neighbourHoodFiltering, initialGlobalAllDifferent);
            for (Map.Entry<Integer, Set<Integer>> entry : inbetween.entrySet()) {
                compatibility[entry.getKey()] = entry.getValue()
                        .stream()
                        .sorted()
                        .collect(Collectors.toList())
                        .toArray(Integer[]::new);
            }
        }
        return compatibility.clone();
    }

    private Integer[][] reverseCompatibility;

    /**
     * Returns the compatibility of each target graph vertex to source graph vertices.
     *
     * @param initialNeighbourhoodFiltering whether to filter the domains of each vertex v such that all candidates have neighbourhoods that can emulate v's neighbourhood.
     * @param initialGlobalAllDifferent     whether to apply AllDifferent to each possible matching to filter out candidates.
     * @return A 2d array compatibility such that for each target vertex with vertex ordering x, compatibility[x] is an array of suitable source graph candidates.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Integer[][] getReverseCompatibility(boolean initialNeighbourhoodFiltering, boolean initialGlobalAllDifferent) {
        if (reverseCompatibility == null) {
            List[] tempReverseCompatibility = new List[targetGraph.vertexSet().size()];
            IntStream.range(0, tempReverseCompatibility.length).forEach(x -> tempReverseCompatibility[x] = new LinkedList());
            Integer[][] compatibility = getCompatibility(initialNeighbourhoodFiltering, initialGlobalAllDifferent);
            for (int sourceVertex = 0; sourceVertex < compatibility.length; sourceVertex++){
                for (int targetVertexIndex = 0; targetVertexIndex < compatibility[sourceVertex].length; targetVertexIndex++) {
                    tempReverseCompatibility[compatibility[sourceVertex][targetVertexIndex]].add(sourceVertex);
                }
            }
            reverseCompatibility = new Integer[targetGraph.vertexSet().size()][];
            for (int i = 0; i< reverseCompatibility.length; i++) {
                reverseCompatibility[i] = (Integer[]) tempReverseCompatibility[i].toArray(Integer[]::new);
            }
        }
        return reverseCompatibility.clone();
    }



    private Integer[][][] targetNeighbours;

    /**
     * Returns an array that provides for each target vertex an ordering in which to try other target vertices in DFS.
     * Since this choice may depend on the target of the DFS, this array incorporates each possible goal vertex.
     * @param strategy the DFS strategy used
     * @return a 3-d array where the first argument is the goal vertex, the second argument is some target graph vertex and the
     * result are neighbours of that vertex in the order that they need to be tried.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public @NotNull Integer[][][] getTargetNeighbours(int strategy) {
        if (targetNeighbours == null) {
            List<Integer> targetVertices = targetGraph.vertexSet()
                    .stream()
                    .sorted().collect(Collectors.toList());
            switch (strategy) {
                case DFS_ARBITRARY:
                    Integer[][] sharedTargetNeighbours = new Integer[targetVertices.size()][];
                    targetNeighbours = new Integer[targetGraph.vertexSet().size()][][];
                    for (int i = 0; i < sharedTargetNeighbours.length; i++) {
                        int candidate = targetVertices.get(i);
                        assert candidate == i : "Target graph does not have consecutive vertex data starting from zero. Index: " + i + ", data: " + candidate;
                        sharedTargetNeighbours[i] = targetGraph.outgoingEdgesOf(candidate)
                                .stream()
                                .map(x -> Graphs.getOppositeVertex(targetGraph, x, candidate))
                                .sorted()
                                .collect(Collectors.toList()).toArray(Integer[]::new);
                    }
                    for (int i = 0; i < targetGraph.vertexSet().size(); i++) {
                        targetNeighbours[i] = Arrays.copyOf(sharedTargetNeighbours, sharedTargetNeighbours.length);
                    }
                    break;
                case DFS_GREEDY:
                    targetNeighbours = getTargetNeighbours(DFS_ARBITRARY);
                    List[][] tempTargetNeigbours = new List[targetNeighbours.length][targetNeighbours.length];
                    for (int i = 0; i < tempTargetNeigbours.length; i++) {
                        for (int j = 0; j < tempTargetNeigbours[i].length; j++) {
                            tempTargetNeigbours[i][j] = new LinkedList(Arrays.asList(targetNeighbours[i][j]));
                        }
                    }
                    ShortestPathAlgorithm<Integer, DefaultEdge> dijkstra = new DijkstraShortestPath<>(targetGraph);
                    for (int i = 0; i < targetNeighbours.length; i++) {
                        int[] distances = new int[tempTargetNeigbours.length];
                        for (int j = 0; j < tempTargetNeigbours[i].length; j++) {
                            List<Integer> toSort = (List<Integer>) tempTargetNeigbours[i][j];
                            Set<Integer> toRemove = new HashSet<>();
                            for (int neighbour : toSort) {
                                GraphPath<Integer, DefaultEdge> path = dijkstra.getPath(neighbour, targetVertices.get(i));
                                if (path == null) {
                                    toRemove.add(neighbour);
                                } else {
                                    distances[neighbour] = path.getLength();
                                }
                            }
                            toSort.removeAll(toRemove);
                            toSort.sort(Comparator.comparingInt(o -> distances[o]));
                            targetNeighbours[i][j] = toSort.toArray(Integer[]::new);
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        return targetNeighbours;
    }


    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtilityData that = (UtilityData) o;
        return targetGraph.equals(that.targetGraph) &&
                patternGraph.equals(that.patternGraph);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetGraph, patternGraph);
    }
}
