package com.charrey.algorithms;

import com.charrey.graph.AttributedVertex;
import com.charrey.util.GraphUtil;
import org.jgrapht.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class GreatestConstrainedFirst<V, E> {


    public List<V> apply(Graph<V , E> graph) {
        List<V> ordering = new ArrayList<>(graph.vertexSet().size());
        if (graph.vertexSet().isEmpty()) {
            return ordering;
        }
        int maxDegree = -1;
        V maxDegreeVertex = null;
        for (V vertex : graph.vertexSet()) {
            int degree = graph.degreeOf(vertex);
            if (degree > maxDegree) {
                maxDegree = degree;
                maxDegreeVertex = vertex;
            }
        }
        ordering.add(maxDegreeVertex);
        while (ordering.size() < graph.vertexSet().size()) {
            Set<V> firstSelection = getFirstCriterium(graph, ordering);
            Set<V> secondSelection = getSecondCriterium(graph, ordering, firstSelection);
            Set<V> thirdSelection = getThirdCriterium(graph, ordering, firstSelection, secondSelection);
            assert secondSelection.containsAll(thirdSelection);
            V toAdd = thirdSelection.iterator().next();
            assert !ordering.contains(toAdd);
            ordering.add(toAdd);
        }

        for (V vertex : graph.vertexSet()) {
            if (vertex instanceof AttributedVertex) {
                ((AttributedVertex) vertex).setId(ordering.indexOf(vertex));
            }
        }

        return Collections.unmodifiableList(ordering);
    }

    private Set<V> getThirdCriterium(Graph<V,E> graph, List<V> ordering, Set<V> firstSelection, Set<V> secondSelection) {
        Set<V> thirdSelection = new HashSet<>();
        long thirdValue = -1;
        for (V vertex : secondSelection) {
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

    private Set<V> getSecondCriterium(Graph<V,E> graph, List<V> ordering, Set<V> firstSelections) {
        Set<V> secondSelection = new HashSet<>();
        long secondValue = -1;
        for (V vertex : firstSelections) {
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

    private Set<V> getFirstCriterium(Graph<V, E> graph, List<V> ordering) {
        Set<V> firstSelection = new HashSet<>();
        long firstValue = -1;
        for (V vertex : graph.vertexSet().stream().filter(x -> !ordering.contains(x)).collect(Collectors.toSet())) {
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
