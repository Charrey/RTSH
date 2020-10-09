package com.charrey.result;

import com.charrey.graph.MyEdge;
import com.charrey.graph.Path;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SuccessResult extends HomeomorphismResult {


    /**
     * The number of iterations the algorithm took to find the homeomorphism. An iteration is one of the following:
     * - Add a vertex matching
     * - Replace a vertex matching by a new one
     * - Remove a vertex matching and all its edge matchings
     * - Add an edge matching
     * - Replace an edge matching by a new one
     * - Remove an edge matching
     */
    private final int[] matching;
    private final Map<MyEdge, Set<Path>> edgeMatching;

    public SuccessResult(int[] matching, Map<MyEdge, Set<Path>> edgeMatching, long iterations, double mem) {
        super(true, iterations, mem);
        this.matching = matching;
        this.edgeMatching = edgeMatching;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Success with vertex-on-vertex matching:\n");
        for (int i = 0; i < matching.length; i++) {
            sb.append(i).append(" matched on ").append(matching[i]).append("\n");
        }
        sb.append("And with edge-on-path matching:\n");
        for (Map.Entry<MyEdge, Set<Path>> entry : edgeMatching.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {
            sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public int[] getVertexPlacement() {
        return matching;
    }

    public Map<MyEdge, Set<Path>> getEdgePlacement() {
        return edgeMatching;
    }
}
