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
                compatibility[entry.getKey().intData()] = entry.getValue()
                        .stream()
                        .sorted(Comparator.comparingInt(Vertex::intData))
                        .collect(Collectors.toList())
                        .toArray(Vertex[]::new);
            }
        }
        return compatibility;
    }


//    private Map<Vertex, Vertex>[] toTryNext;
//    public Map<Vertex, Vertex>[] getToTryNext() {
//        if (toTryNext == null) {
//            toTryNext = GraphUtil.getToTryNext(getOrder(), getCompatibility());
//        }
//        return toTryNext;
//    }


    private Vertex[][] targetNeighbours;
    public Vertex[][] getTargetNeighbours() {
        if (targetNeighbours == null) {
            List<Vertex> routing = targetGraph.vertexSet()
                    .stream()
                    .sorted(Comparator.comparingInt(Vertex::intData)).collect(Collectors.toList());
            targetNeighbours = new Vertex[routing.size()][];
            for (int i = 0; i < targetNeighbours.length; i++) {
                targetNeighbours[i] = GraphUtil.neighboursOf(targetGraph, routing.get(i))
                        .stream()
                        .sorted(Comparator.comparingInt(Vertex::intData))
                        .collect(Collectors.toList()).toArray(Vertex[]::new);
            }


        }
        return targetNeighbours;
    }


    List<Vertex> compatibleValues;
    public List<Vertex> getCompatibleValues() {
        if (compatibleValues == null) {
            Set<Vertex> compatibleValuesSet = new HashSet<>();
            for (Vertex[] vertices : compatibility) {
                compatibleValuesSet.addAll(Arrays.asList(vertices));
            }
            compatibleValues = new LinkedList<>(compatibleValuesSet);
            compatibleValues.sort(Comparator.comparingInt(Vertex::intData));
            compatibleValues = Collections.unmodifiableList(compatibleValues);
        }
        return compatibleValues;
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
