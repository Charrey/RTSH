package com.charrey.algorithms;

import com.charrey.graph.MyGraph;
import com.charrey.util.GraphUtil;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Returns a vertex order using the GreatestConstraintFirst algorithm. This vertex order attempts to have each
 * * consecutive vertex be connected with as many already already matched vertices possible.
 */
public class GreatestConstrainedFirst {


    /**
     * Returns a source graph vertex ordering to apply homeomorphism finding to. This vertex order attempts to have each
     * consecutive vertex be connected with as many already already matched vertices possible.
     *
     * @param graph the source graph
     * @return an appropriate vertex ordering.
     */
    @NotNull
    public Mapping apply(@NotNull MyGraph graph) {
        List<Integer> ordering = new ArrayList<>(graph.vertexSet().size());
        if (graph.vertexSet().isEmpty()) {
            return new Mapping(graph, new int[0], new int[0]);
        }
        int maxDegree = -1;
        int maxDegreeVertex = -1;
        for (int vertex : graph.vertexSet()) {
            int degree = graph.degreeOf(vertex);
            if (degree > maxDegree) {
                maxDegree = degree;
                maxDegreeVertex = vertex;
            }
        }
        ordering.add(maxDegreeVertex);
        while (ordering.size() < graph.vertexSet().size()) {
            Set<Integer> firstSelection = getFirstCriterium(graph, ordering);
            Set<Integer> secondSelection = getSecondCriterium(graph, ordering, firstSelection);
            List<Integer> thirdSelection = new LinkedList<>(getThirdCriterium(graph, ordering, secondSelection));
            thirdSelection.sort(Integer::compareTo);
            assert secondSelection.containsAll(thirdSelection);
            int toAdd = thirdSelection.get(0);
            assert !ordering.contains(toAdd);
            ordering.add(toAdd);
        }
        int[] reverseOrdering = new int[ordering.size()];
        for (int i = 0; i < ordering.size(); i++) {
            reverseOrdering[ordering.get(i)] = i;
        }
        int[] orderingAsArray = ordering.stream().mapToInt(x -> x).toArray();
        return new Mapping(MyGraph.applyOrdering(graph, orderingAsArray, reverseOrdering), reverseOrdering, orderingAsArray);
    }

    @NotNull
    private Set<Integer> getThirdCriterium(@NotNull MyGraph graph, @NotNull List<Integer> ordering, @NotNull Set<Integer> secondSelection) {
        Set<Integer> thirdSelection = new HashSet<>();
        long thirdValue = -1;
        for (Integer vertex : secondSelection) {
            long score = GraphUtil.neighboursOf(graph,
                    Graphs.neighborSetOf(graph, vertex)
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
    private Set<Integer> getSecondCriterium(@NotNull MyGraph graph, @NotNull List<Integer> ordering, @NotNull Set<Integer> firstSelections) {
        Set<Integer> secondSelection = new HashSet<>();
        long secondValue = -1;
        for (Integer vertex : firstSelections) {
            long score = GraphUtil.neighboursOf(graph,
                    Graphs.neighborSetOf(graph, vertex)
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
    private Set<Integer> getFirstCriterium(@NotNull MyGraph graph, @NotNull List<Integer> ordering) {
        Set<Integer> firstSelection = new HashSet<>();
        long firstValue = -1;
        for (Integer vertex : graph.vertexSet().stream().filter(x -> !ordering.contains(x)).collect(Collectors.toSet())) {
            long score = Graphs.neighborSetOf(graph, vertex).stream()
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

    public static class Mapping {
        public final int[] new_to_old;
        public final int[] old_to_new;
        public final MyGraph graph;

        Mapping(MyGraph graph, int[] new_to_old, int[] old_to_new) {
            this.graph = graph;
            this.new_to_old = new_to_old;
            this.old_to_new = old_to_new;
        }
    }
}
