package com.charrey.algorithms;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.settings.PathIterationConstants;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import com.charrey.settings.pruning.domainfilter.LabelDegreeFiltering;
import com.charrey.util.Util;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.set.TIntSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;

import java.util.*;
import java.util.stream.Collectors;

import static com.charrey.settings.PathIterationConstants.DFS_ARBITRARY;

/**
 * Class that provides several properties of graphs that are often used and difficult to calculate.
 */
public class UtilityData {
    private final MyGraph targetGraph;
    private final MyGraph patternGraph;

    public MyGraph getTargetGraph() {
        return targetGraph;
    }

    public MyGraph getPatternGraph() {
        return patternGraph;
    }

    /**
     * Instantiates a new Utility data class. This needs to be done (instead of static calls) to allow for caching.
     *
     * @param sourceGraph the source graph
     * @param targetGraph the target graph
     */
    public UtilityData(MyGraph sourceGraph, MyGraph targetGraph) {
        this.patternGraph = sourceGraph;
        this.targetGraph = targetGraph;
    }

    /**
     * Returns the number of vertices in the target graph
     *
     * @return the number of vertices in the target graph
     */
    public int targetGraphSize() {
        return targetGraph.vertexSet().size();
    }

    /**
     * Returns the number of vertices in the source graph
     *
     * @return the number of vertices in the source graph
     */
    public int sourceGraphSize() {
        return patternGraph.vertexSet().size();
    }


    private int[][] compatibility;

    /**
     * Returns the compatibility of each source graph vertex to target graph vertices.
     * <p>
     *
     * @param filteringSettings whether to filter the domains of each vertex v such that all candidates have neighbourhoods that can emulate v's neighbourhood.
     * @param name              name to print when displaying compability filtering progress
     * @return A 2d array compatibility such that for each source vertex with vertex ordering x, compatibility[x] is an array of suitable target graph candidates.
     */
    public int[][] getCompatibility(FilteringSettings filteringSettings, String name) {
        if (compatibility == null) {
            compatibility = new int[patternGraph.vertexSet().size()][];
            TIntObjectMap<TIntSet> inbetween = new CompatibilityChecker().get(patternGraph, targetGraph, filteringSettings instanceof LabelDegreeFiltering, name);
            inbetween.forEachEntry((key, value) -> {
                int[] array = value.toArray();
                Arrays.sort(array);
                compatibility[key] = array;
                return true;
            });
        }
        return compatibility.clone();
    }


    private int[][][] targetNeighbours;

    private final RandomGenerator random = new Well512a(49999);

    private Map<Integer, Set<Integer>> unconfigurableCover;


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

    /**
     * Returns an array that provides for each target vertex an ordering in which to try other target vertices in DFS.
     * Since this choice may depend on the target of the DFS, this array incorporates each possible goal vertex.
     *
     * @param strategy the DFS strategy used
     * @return a 3-d array where the first argument is the goal vertex, the second argument is some target graph vertex and the result are neighbours of that vertex in the order that they need to be tried.
     */
    @SuppressWarnings({"rawtypes", "unchecked", "AssignmentOrReturnOfFieldWithMutableType"})
    public @NotNull
    int[][][] getTargetNeighbours(PathIterationConstants strategy) {
        if (targetNeighbours == null) {
            List<Integer> targetVertices = targetGraph.vertexSet()
                    .stream()
                    .sorted().collect(Collectors.toList());
            switch (strategy) {
                case DFS_ARBITRARY:
                    int[][] sharedTargetNeighbours = new int[targetVertices.size()][];
                    targetNeighbours = new int[targetGraph.vertexSet().size()][][];
                    for (int i = 0; i < sharedTargetNeighbours.length; i++) {
                        int candidate = targetVertices.get(i);
                        assert candidate == i : "Target graph does not have consecutive vertex data starting from zero. Index: " + i + ", data: " + candidate;
                        sharedTargetNeighbours[i] = targetGraph.outgoingEdgesOf(candidate)
                                .stream()
                                .mapToInt(x -> Graphs.getOppositeVertex(targetGraph, x, candidate))
                                .toArray();
                        Util.shuffle(sharedTargetNeighbours[i], random);
                    }
                    for (int i = 0; i < targetGraph.vertexSet().size(); i++) {
                        targetNeighbours[i] = Arrays.copyOf(sharedTargetNeighbours, sharedTargetNeighbours.length);
                    }
                    break;
                case DFS_GREEDY:
                    targetNeighbours = getTargetNeighbours(DFS_ARBITRARY);
                    ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<Integer, MyEdge> shortestPaths = new CHManyToManyShortestPaths<>(targetGraph).getManyToManyPaths(targetGraph.vertexSet(), targetGraph.vertexSet());

                    List[][] tempTargetNeigbours = new List[targetNeighbours.length][targetNeighbours.length];
                    for (int i = 0; i < tempTargetNeigbours.length; i++) {
                        for (int j = 0; j < tempTargetNeigbours[i].length; j++) {
                            tempTargetNeigbours[i][j] = Arrays.stream(targetNeighbours[i][j]).boxed().collect(Collectors.toList());
                        }
                    }
                    for (int goal = 0; goal < targetNeighbours.length; goal++) {
                        double[] distances = new double[tempTargetNeigbours.length];
                        for (int from = 0; from < tempTargetNeigbours[goal].length; from++) {
                            List<Integer> toSort = tempTargetNeigbours[goal][from];
                            Set<Integer> toRemove = new HashSet<>();
                            for (int to : toSort) {
                                GraphPath<Integer, MyEdge> path = shortestPaths.getPath(to, targetVertices.get(goal));
                                if (path == null) {
                                    toRemove.add(to);
                                } else {
                                    distances[to] = targetGraph.getEdgeWeight(targetGraph.getEdge(from, to)) + path.getWeight();
                                }
                            }
                            toSort.removeAll(toRemove);
                            toSort.sort(Comparator.comparingDouble(o -> distances[o]));
                            targetNeighbours[goal][from] = toSort.stream().mapToInt(x -> x).toArray();
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        return targetNeighbours;
    }

    public Set<Integer> unconfigurableCover(int of) {
        if (unconfigurableCover == null) {
            unconfigurableCover = new HashMap<>();
            for (int vertex : targetGraph.vertexSet()) {
                Set<Integer> result = new HashSet<>();
                Deque<Integer> frontier = new LinkedList<>();
                frontier.add(vertex);
                while (!frontier.isEmpty()) {
                    int observingVertex = frontier.pop();
                    Set<Integer> neighbours = Graphs.neighborSetOf(targetGraph, observingVertex);
                    for (int neighbour : neighbours) {
                        if (targetGraph.getAttributes(neighbour).containsKey("configurable") && targetGraph.getAttributes(neighbour).get("configurable").equals(Set.of("0")) && !result.contains(neighbour)) {
                            frontier.add(neighbour);
                            Graphs.neighborSetOf(targetGraph, neighbour).stream()
                                    .filter(x -> targetGraph.getAttributes(x).get("label").contains("wire"))
                                    .forEach(integer -> {
                                        if (!result.contains(integer)) {
                                            frontier.add(integer);
                                        }
                                    });
                        }
                    }
                    result.add(observingVertex);
                }
                if (result.size() > 1) {
                    unconfigurableCover.put(vertex, result);
                }

            }
        }
        return unconfigurableCover.getOrDefault(of, Set.of(of));
    }
}
