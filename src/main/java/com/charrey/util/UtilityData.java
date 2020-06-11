package com.charrey.util;

import com.charrey.algorithms.CompatibilityChecker;
import com.charrey.algorithms.GreatestConstrainedFirst;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
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

public class UtilityData {
    private final MyGraph targetGraph;
    private final MyGraph patternGraph;

    public UtilityData(MyGraph patternGraph, MyGraph targetGraph) {
        this.patternGraph = patternGraph;
        this.targetGraph = targetGraph;
    }

    private List<Vertex> order;
    public List<Vertex> getOrder() {
        if (order == null) {
            order = new GreatestConstrainedFirst().apply(patternGraph);
        }
        return order;
    }

    private Vertex[][] compatibility;
    public Vertex[][] getCompatibility() {
        if (compatibility == null) {
            compatibility = new Vertex[getOrder().size()][];
            Map<Vertex, Set<Vertex>> inbetween = new CompatibilityChecker().get(patternGraph, targetGraph);
            for (Map.Entry<Vertex, Set<Vertex>> entry : inbetween.entrySet()) {
                compatibility[entry.getKey().data()] = entry.getValue()
                        .stream()
                        .sorted(Comparator.comparingInt(Vertex::data))
                        .collect(Collectors.toList())
                        .toArray(Vertex[]::new);
            }
        }
        return compatibility;
    }

    private Vertex[][] reverseCompatibility;
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Vertex[][] getReverseCompatibility() {
        if (reverseCompatibility == null) {
            List[] tempReverseCompatibility = new List[targetGraph.vertexSet().size()];
            IntStream.range(0, tempReverseCompatibility.length).forEach(x -> tempReverseCompatibility[x] = new LinkedList());
            Vertex[][] compatibility = getCompatibility();
            for (int sourceVertex = 0; sourceVertex < compatibility.length; sourceVertex++){
                for (int targetVertexIndex = 0; targetVertexIndex < compatibility[sourceVertex].length; targetVertexIndex++) {
                    tempReverseCompatibility[compatibility[sourceVertex][targetVertexIndex].data()].add(getOrder().get(sourceVertex));
                }
            }
            reverseCompatibility = new Vertex[targetGraph.vertexSet().size()][];
            for (int i = 0; i< reverseCompatibility.length; i++) {
                reverseCompatibility[i] = (Vertex[]) tempReverseCompatibility[i].toArray(Vertex[]::new);
            }
        }
        return reverseCompatibility;
    }



    private Vertex[][][] targetNeighbours;
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Vertex[][][] getTargetNeighbours(int strategy) {
        if (targetNeighbours == null) {
            List<Vertex> targetVertices = targetGraph.vertexSet()
                    .stream()
                    .sorted().collect(Collectors.toList());
            switch (strategy) {
                case DFS_ARBITRARY:
                    Vertex[][] sharedTargetNeighbours = new Vertex[targetVertices.size()][];
                    targetNeighbours = new Vertex[targetGraph.vertexSet().size()][][];
                    for (int i = 0; i < sharedTargetNeighbours.length; i++) {
                        Vertex candidate = targetVertices.get(i);
                        assert candidate.data() == i;
                        sharedTargetNeighbours[i] = targetGraph.outgoingEdgesOf(candidate)
                                .stream()
                                .map(x -> Graphs.getOppositeVertex(targetGraph, x, candidate))
                                .sorted(Comparator.comparingInt(Vertex::data))
                                .collect(Collectors.toList()).toArray(Vertex[]::new);
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
                    ShortestPathAlgorithm<Vertex, DefaultEdge> dijkstra = new DijkstraShortestPath<>(targetGraph);
                    for (int i = 0; i < targetNeighbours.length; i++) {
                        int[] distances = new int[tempTargetNeigbours.length];
                        for (int j = 0; j < tempTargetNeigbours[i].length; j++) {
                            List<Vertex> toSort = (List<Vertex>) tempTargetNeigbours[i][j];
                            Set<Vertex> toRemove = new HashSet<>();
                            for (Vertex neighbour : toSort) {
                                GraphPath<Vertex, DefaultEdge> path = dijkstra.getPath(neighbour, targetVertices.get(i));
                                if (path == null) {
                                    toRemove.add(neighbour);
                                } else {
                                    distances[neighbour.data()] = path.getLength();
                                }
                            }
                            toSort.removeAll(toRemove);
                            toSort.sort(Comparator.comparingInt(o -> distances[o.data()]));
                            targetNeighbours[i][j] = toSort.toArray(Vertex[]::new);
                        }
                    }
            }
        }
        return targetNeighbours;
    }


    @Override
    public boolean equals(Object o) {
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
