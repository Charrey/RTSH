package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import com.charrey.util.GraphUtil;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns a vertex order using the GreatestConstraintFirst algorithm. This vertex order attempts to have each
 * * consecutive vertex be connected with as many already already matched vertices possible.
 */
public class GreatestConstrainedFirst implements GraphVertexMapper {


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
        TIntList newToOld = new TIntArrayList(graph.vertexSet().size());
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
        newToOld.add(maxDegreeVertex);
        while (newToOld.size() < graph.vertexSet().size()) {
            TIntIntMap score1 = getScore1(graph, newToOld);
            TIntIntMap score2 = getScore2(graph, newToOld);
            TIntIntMap score3 = getScore3(graph, newToOld, score1, score2);
            List<Integer> allVertices = graph.vertexSet().stream().filter(x -> !newToOld.contains(x)).sorted(Comparator.comparingInt(o -> -score1.get((Integer) o))
                    .thenComparingInt(o -> -score2.get((Integer) o))
                    .thenComparingInt(o -> -score3.get((Integer) o))
                    .thenComparingInt(o -> (Integer) o)).collect(Collectors.toList());

            int toAdd = allVertices.get(0);
            assert !newToOld.contains(toAdd);
            newToOld.add(toAdd);
        }
        int[] oldToNew = new int[newToOld.size()];
        for (int i = 0; i < newToOld.size(); i++) {
            oldToNew[newToOld.get(i)] = i;
        }
        int[] newToOldArray = newToOld.toArray();
        return new Mapping(MyGraph.applyOrdering(graph, newToOldArray, oldToNew), newToOldArray);
    }

    private TIntIntMap getScore3(MyGraph graph, TIntList ordering, TIntIntMap score1, TIntIntMap score2) {
        TIntIntMap res = new TIntIntHashMap();
        graph.vertexSet().forEach(integer -> {
            if (!ordering.contains(integer)) {
                res.put(integer, Graphs.neighborSetOf(graph, integer).size() - (score1.get(integer) + score2.get(integer)));
            }
        });
        return res;
    }

    private TIntIntMap getScore2(MyGraph graph, TIntList ordering) {
        TIntIntMap res = new TIntIntHashMap();
        graph.vertexSet().forEach(integer -> {
            if (!ordering.contains(integer)) {
                res.put(integer, (int) Graphs.neighborSetOf(graph, integer).stream().filter(x -> !ordering.contains(x) && Graphs.neighborSetOf(graph, x).stream().anyMatch(ordering::contains)).count());
            }
        });
        return res;
    }

    private TIntIntMap getScore1(MyGraph graph, TIntList ordering) {
        TIntIntMap res = new TIntIntHashMap();
        graph.vertexSet().forEach(integer -> {
            if (!ordering.contains(integer)) {
                res.put(integer, (int) Graphs.neighborSetOf(graph, integer).stream().filter(ordering::contains).count());
            }
        });
        return res;
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
