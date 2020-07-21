package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import com.charrey.util.GraphUtil;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;

import java.util.stream.Collectors;

/**
 * Returns a vertex order using the GreatestConstraintFirst algorithm. This vertex order attempts to have each
 * * consecutive vertex be connected with as many already already matched vertices possible.
 */
public class GreatestConstrainedFirst extends GraphVertexMapper {


    /**
     * Returns a source graph vertex ordering to apply homeomorphism finding to. This vertex order attempts to have each
     * consecutive vertex be connected with as many already already matched vertices possible.
     *
     * @param graph the source graph
     * @return an appropriate vertex ordering.
     */
    @NotNull
    @Override
    public Mapping apply(@NotNull MyGraph graph) {
        TIntList new_to_old = new TIntArrayList(graph.vertexSet().size());
        if (graph.vertexSet().isEmpty()) {
            return new Mapping(graph, new int[0]);
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
        new_to_old.add(maxDegreeVertex);
        while (new_to_old.size() < graph.vertexSet().size()) {
            TIntSet firstSelection = getFirstCriterium(graph, new_to_old);
            TIntSet secondSelection = getSecondCriterium(graph, new_to_old, firstSelection);
            TIntList thirdSelection = new TIntLinkedList(getThirdCriterium(graph, new_to_old, secondSelection));
            thirdSelection.sort();
            assert secondSelection.containsAll(thirdSelection);
            int toAdd = thirdSelection.get(0);
            assert !new_to_old.contains(toAdd);
            new_to_old.add(toAdd);
        }
        int[] old_to_new = new int[new_to_old.size()];
        for (int i = 0; i < new_to_old.size(); i++) {
            old_to_new[new_to_old.get(i)] = i;
        }
        int[] new_to_old_array = new_to_old.toArray();
        return new Mapping(MyGraph.applyOrdering(graph, new_to_old_array, old_to_new), new_to_old_array);
    }

    @NotNull
    private static TIntList getThirdCriterium(@NotNull MyGraph graph, @NotNull TIntList ordering, @NotNull TIntSet secondSelection) {
        TIntList thirdSelection = new TIntLinkedList();
        final long[] thirdValue = {-1};
        secondSelection.forEach(vertex -> {
            final long[] score = {0};

            GraphUtil.neighboursOf(graph,
                    Graphs.neighborSetOf(graph, vertex)
                            .stream()
                            .filter(x -> !ordering.contains(x))
                            .collect(Collectors.toSet())).forEach(i -> {
                if (!ordering.contains(i)) {
                    score[0]++;
                }
                return true;
            });

            if (score[0] > thirdValue[0]) {
                thirdSelection.clear();
                thirdSelection.add(vertex);
                thirdValue[0] = score[0];
            } else if (score[0] == thirdValue[0]) {
                thirdSelection.add(vertex);
            }
            return true;
        });
        return thirdSelection;
    }

    @NotNull
    private static TIntSet getSecondCriterium(@NotNull MyGraph graph, @NotNull TIntList ordering, @NotNull TIntSet firstSelections) {
        TIntSet secondSelection = new TIntHashSet();
        final long[] secondValue = {-1};

        firstSelections.forEach(vertex -> {
            final long[] score = {0};

            GraphUtil.neighboursOf(graph,
                    Graphs.neighborSetOf(graph, vertex)
                            .stream()
                            .filter(x -> !ordering.contains(x))
                            .collect(Collectors.toSet())).forEach(i -> {
                if (ordering.contains(i)) {
                    score[0]++;
                }
                return true;
            });
            if (score[0] > secondValue[0]) {
                secondSelection.clear();
                secondSelection.add(vertex);
                secondValue[0] = score[0];
            } else if (score[0] == secondValue[0]) {
                secondSelection.add(vertex);
            }
            return true;
        });
        return secondSelection;
    }

    @NotNull
    private static TIntSet getFirstCriterium(@NotNull MyGraph graph, @NotNull TIntList ordering) {
        TIntSet firstSelection = new TIntHashSet();
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
}
