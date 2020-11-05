package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import com.charrey.util.GraphUtil;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;

import java.util.*;
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
        List<Integer> newToOld = new ArrayList<>(graph.vertexSet().size());
        if (graph.vertexSet().isEmpty()) {
            return new Mapping(graph, new HashMap<>());
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
        Map<Integer, Integer> newToOldArray = new HashMap<>();
        for (int i = 0; i < newToOld.size(); i++) {
            newToOldArray.put(i, newToOld.get(i));
        }
        return new Mapping(MyGraph.applyOrdering(graph, newToOldArray, oldToNew), newToOldArray);
    }

    private TIntIntMap getScore3(MyGraph graph, List<Integer> ordering, TIntIntMap score1, TIntIntMap score2) {
        TIntIntMap res = new TIntIntHashMap();
        graph.vertexSet().forEach(integer -> {
            if (!ordering.contains(integer)) {
                res.put(integer, Graphs.neighborSetOf(graph, integer).size() - (score1.get(integer) + score2.get(integer)));
            }
        });
        return res;
    }

    private TIntIntMap getScore2(MyGraph graph, List<Integer> ordering) {
        TIntIntMap res = new TIntIntHashMap();
        graph.vertexSet().forEach(integer -> {
            if (!ordering.contains(integer)) {
                res.put(integer, (int) Graphs.neighborSetOf(graph, integer).stream().filter(x -> !ordering.contains(x) && Graphs.neighborSetOf(graph, x).stream().anyMatch(ordering::contains)).count());
            }
        });
        return res;
    }

    private TIntIntMap getScore1(MyGraph graph, List<Integer> ordering) {
        TIntIntMap res = new TIntIntHashMap();
        graph.vertexSet().forEach(integer -> {
            if (!ordering.contains(integer)) {
                res.put(integer, (int) Graphs.neighborSetOf(graph, integer).stream().filter(ordering::contains).count());
            }
        });
        return res;
    }

}
