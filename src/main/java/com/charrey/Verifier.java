package com.charrey;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Class that verifies correctness of a homeomorphism.
 */
class Verifier {

    /**
     * Returns whether a homeomorphism adheres to the mathematical definition and constraints.
     *
     * @param sourceGraph    the source graph
     * @param vertexMatching the vertex-on-vertex matching
     * @param edgeMatching   the edge-on-path matching
     * @return whether the homeomorphism is correct
     */
    static boolean isCorrect(@NotNull MyGraph sourceGraph, @NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
        //all nodes are placed
        if (vertexMatching.getPlacement().size() < sourceGraph.vertexSet().size()) {
            return false;
        }
        //all nodes are distinct
        if (vertexMatching.getPlacement().size() != new TIntHashSet(vertexMatching.getPlacement()).size()) {
            return false;
        }

        //all edges are placed
        if (edgeMatching.allPaths().size() != sourceGraph.edgeSet().size()) {
            return false;
        }
        if (sourceGraph.isDirected()) {
            for (MyEdge edge : sourceGraph.edgeSet()) {
                int edgeSourceTarget = vertexMatching.getPlacement().get(sourceGraph.getEdgeSource(edge));
                int edgeTargetTarget = vertexMatching.getPlacement().get(sourceGraph.getEdgeTarget(edge));
                long matches = edgeMatching.allPaths().stream().filter(x -> x.last() == edgeTargetTarget && x.first() == edgeSourceTarget).count();
                if (matches != 1) {
                    return false;
                }
            }
        } else {
            for (MyEdge edge : sourceGraph.edgeSet()) {
                int edgeSourceTarget = vertexMatching.getPlacement().get(sourceGraph.getEdgeSource(edge));
                int edgeTargetTarget = vertexMatching.getPlacement().get(sourceGraph.getEdgeTarget(edge));
                long matches = edgeMatching.allPaths().stream().filter(x -> Set.of(x.last(), x.first()).equals(Set.of(edgeSourceTarget, edgeTargetTarget))).count();
                if (matches != 1) {
                    return false;
                }
            }
        }

        //the intermediate list of nodes are distinct
        for (Path path : edgeMatching.allPaths()) {
            List<Integer> intermediate = path.intermediate();
            if (!edgeMatching.allPaths().stream().allMatch(x -> x == path || x.intermediate().stream().noneMatch(intermediate::contains))) {
                return false;
            }
        }

        //the intermediate list of nodes are disjoint from the nodes
        for (Path path : edgeMatching.allPaths()) {
            List<Integer> intermediate = path.intermediate();
            final boolean[] toReturnFalse = {false};
            vertexMatching.getPlacement().forEach(i -> {
                if (intermediate.contains(i)) {
                    toReturnFalse[0] = true;
                    return false;
                }
                return true;
            });
            if (toReturnFalse[0]) {
                return false;
            }
        }
        return true;
    }
}
