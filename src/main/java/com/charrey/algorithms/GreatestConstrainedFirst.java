package com.charrey.algorithms;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.util.GraphUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GreatestConstrainedFirst {


    @NotNull
    public List<Vertex> apply(@NotNull MyGraph graph) {
        List<Vertex> ordering = new ArrayList<>(graph.vertexSet().size());
        if (graph.vertexSet().isEmpty()) {
            return ordering;
        }
        int maxDegree = -1;
        Vertex maxDegreeVertex = null;
        for (Vertex vertex : graph.vertexSet()) {
            int degree = graph.degreeOf(vertex);
            if (degree > maxDegree) {
                maxDegree = degree;
                maxDegreeVertex = vertex;
            }
        }
        ordering.add(maxDegreeVertex);
        while (ordering.size() < graph.vertexSet().size()) {
            Set<Vertex> firstSelection = getFirstCriterium(graph, ordering);
            Set<Vertex> secondSelection = getSecondCriterium(graph, ordering, firstSelection);
            List<Vertex> thirdSelection = new LinkedList<>(getThirdCriterium(graph, ordering, secondSelection));
            thirdSelection.sort(Comparator.comparingInt(Vertex::data));
            assert secondSelection.containsAll(thirdSelection);
            Vertex toAdd = thirdSelection.get(0);
            assert !ordering.contains(toAdd);
            ordering.add(toAdd);
        }

        for (Vertex vertex : graph.vertexSet()) {
            vertex.setData(ordering.indexOf(vertex));
        }

        return Collections.unmodifiableList(ordering);
    }

    @NotNull
    private Set<Vertex> getThirdCriterium(@NotNull MyGraph graph, @NotNull List<Vertex> ordering, @NotNull Set<Vertex> secondSelection) {
        Set<Vertex> thirdSelection = new HashSet<>();
        long thirdValue = -1;
        for (Vertex vertex : secondSelection) {
            long score = GraphUtil.neighboursOf(graph,
                    GraphUtil.neighboursOf(graph, vertex)
                        .stream()
                        .filter(x -> !ordering.contains(x))
                        .collect(Collectors.toSet()))
                    .stream()
                    .filter(x -> !ordering.contains(x))
                    .count();
            if (score > thirdValue) {
                thirdSelection.clear();
                thirdSelection.add(vertex);
                thirdValue = score;
            } else if (score == thirdValue) {
                thirdSelection.add(vertex);
            }
        }
        return thirdSelection;
    }

    @NotNull
    private Set<Vertex> getSecondCriterium(@NotNull MyGraph graph, @NotNull List<Vertex> ordering, @NotNull Set<Vertex> firstSelections) {
        Set<Vertex> secondSelection = new HashSet<>();
        long secondValue = -1;
        for (Vertex vertex : firstSelections) {
            long score = GraphUtil.neighboursOf(graph,
                    GraphUtil.neighboursOf(graph, vertex)
                                   .stream()
                                   .filter(x -> !ordering.contains(x))
                                   .collect(Collectors.toSet()))
                    .stream()
                    .filter(ordering::contains)
                    .count();
            if (score > secondValue) {
                secondSelection.clear();
                secondSelection.add(vertex);
                secondValue = score;
            } else if (score == secondValue) {
                secondSelection.add(vertex);
            }
        }
        return secondSelection;
    }

    @NotNull
    private Set<Vertex> getFirstCriterium(@NotNull MyGraph graph, @NotNull List<Vertex> ordering) {
        Set<Vertex> firstSelection = new HashSet<>();
        long firstValue = -1;
        for (Vertex vertex : graph.vertexSet().stream().filter(x -> !ordering.contains(x)).collect(Collectors.toSet())) {
            long score = GraphUtil.neighboursOf(graph, vertex).stream()
                    .filter(ordering::contains)
                    .count();
            if (score > firstValue) {
                firstSelection.clear();
                firstSelection.add(vertex);
                firstValue = score;
            } else if (score == firstValue) {
                firstSelection.add(vertex);
            }
        }
        return firstSelection;
    }
}
