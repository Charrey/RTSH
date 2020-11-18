package com.charrey.algorithms;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.ReadOnlyOccupation;
import gnu.trove.TIntCollection;
import gnu.trove.procedure.TIntProcedure;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;

import java.util.*;

public class RefuseLongerPaths {


    public static boolean hasUnnecessarilyLongPaths(final MyGraph graph, @NotNull Path pathFound) {
        for (int i = 0; i < pathFound.length() - 2; i++) {
            int from = pathFound.get(i);
            int head = pathFound.get(i+1);
            List<Integer> to = new ArrayList<>();
            pathFound.asList().subList(i + 2, pathFound.length()).forEach(new TIntProcedure() {
                boolean inPrePhase = true;
                @Override
                public boolean execute(int value) {
                    if (inPrePhase && graph.degreeOf(value) == 2) {
                        return true;
                    } else {
                        inPrePhase = false;
                        to.add(value);
                        return true;
                    }
                }
            });
            if (to.stream().anyMatch(x -> graph.getAllEdges(from, x).size() > 0)) {
                return true;
            } else if (Graphs.successorListOf(graph, from)
                    .stream()
                    .filter(x -> graph.getLabels(x).contains("port") ||  graph.getLabels(x).contains("arc"))
                    .filter(x -> graph.inDegreeOf(x) == 1)
                    .filter(x -> graph.outDegreeOf(x) == 1)
                    .filter(x -> x != head)
                    .map(x -> Graphs.successorListOf(graph, x).iterator().next())
                    .anyMatch(to::contains)) {
                return true;
            }
        }
        return false;
    }
}
