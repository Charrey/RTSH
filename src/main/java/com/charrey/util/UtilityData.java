package com.charrey.util;

import com.charrey.algorithms.CompatibilityChecker;
import com.charrey.algorithms.GreatestConstrainedFirst;
import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class UtilityData {
    private final Graph<Vertex, DefaultEdge> targetGraph;
    private final Graph<Vertex, DefaultEdge> patternGraph;

    public UtilityData(Graph<Vertex, DefaultEdge> patternGraph, Graph<Vertex, DefaultEdge> targetGraph) {
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



    private Vertex[][] targetNeighbours;
    public Vertex[][] getTargetNeighbours() {
        if (targetNeighbours == null) {
            List<Vertex> routing = targetGraph.vertexSet()
                    .stream()
                    .sorted(Comparator.comparingInt(Vertex::data)).collect(Collectors.toList());
            targetNeighbours = new Vertex[routing.size()][];
            for (int i = 0; i < targetNeighbours.length; i++) {
                targetNeighbours[i] = GraphUtil.neighboursOf(targetGraph, routing.get(i))
                        .stream()
                        .sorted(Comparator.comparingInt(Vertex::data))
                        .collect(Collectors.toList()).toArray(Vertex[]::new);
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
