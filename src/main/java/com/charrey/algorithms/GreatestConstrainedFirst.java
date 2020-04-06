package com.charrey.algorithms;

import com.charrey.graph.AttributedVertex;
import com.charrey.util.GraphUtil;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

public class GreatestConstrainedFirst {


    public List<AttributedVertex> apply(Graph<AttributedVertex , DefaultEdge> graph) {
        List<AttributedVertex> ordering = new ArrayList<>(graph.vertexSet().size());
        if (graph.vertexSet().isEmpty()) {
            return ordering;
        }
        int maxDegree = -1;
        AttributedVertex maxDegreeVertex = null;
        for (AttributedVertex vertex : graph.vertexSet()) {
            int degree = graph.degreeOf(vertex);
            if (degree > maxDegree) {
                maxDegree = degree;
                maxDegreeVertex = vertex;
            }
        }
        ordering.add(maxDegreeVertex);
        while (ordering.size() < graph.vertexSet().size()) {
            Set<AttributedVertex> firstSelection = getFirstCriterium(graph, ordering);
            Set<AttributedVertex> secondSelection = getSecondCriterium(graph, ordering, firstSelection);
            Set<AttributedVertex> thirdSelection = getThirdCriterium(graph, ordering, firstSelection, secondSelection);
            assert secondSelection.containsAll(thirdSelection);
            AttributedVertex toAdd = thirdSelection.iterator().next();
            assert !ordering.contains(toAdd);
            ordering.add(toAdd);
        }

        for (AttributedVertex vertex : graph.vertexSet()) {
            vertex.setData(ordering.indexOf(vertex));
        }

        return Collections.unmodifiableList(ordering);
    }

    private Set<AttributedVertex> getThirdCriterium(Graph<AttributedVertex, DefaultEdge> graph, List<AttributedVertex> ordering, Set<AttributedVertex> firstSelection, Set<AttributedVertex> secondSelection) {
        Set<AttributedVertex> thirdSelection = new HashSet<>();
        long thirdValue = -1;
        for (AttributedVertex vertex : secondSelection) {
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

    private Set<AttributedVertex> getSecondCriterium(Graph<AttributedVertex, DefaultEdge> graph, List<AttributedVertex> ordering, Set<AttributedVertex> firstSelections) {
        Set<AttributedVertex> secondSelection = new HashSet<>();
        long secondValue = -1;
        for (AttributedVertex vertex : firstSelections) {
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

    private Set<AttributedVertex> getFirstCriterium(Graph<AttributedVertex, DefaultEdge> graph, List<AttributedVertex> ordering) {
        Set<AttributedVertex> firstSelection = new HashSet<>();
        long firstValue = -1;
        for (AttributedVertex vertex : graph.vertexSet().stream().filter(x -> !ordering.contains(x)).collect(Collectors.toSet())) {
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
