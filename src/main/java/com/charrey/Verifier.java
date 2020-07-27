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

    private Verifier() {
    }

    /**
     * Returns whether a homeomorphism adheres to the mathematical definition and constraints.
     *
     * @param sourceGraph    the source graph
     * @param vertexMatching the vertex-on-vertex matching
     * @param edgeMatching   the edge-on-path matching
     * @return whether the homeomorphism is correct
     */
    static boolean isCorrect(@NotNull MyGraph sourceGraph, @NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
        return everyVertexPlaced(sourceGraph, vertexMatching) &&
                uniqueTargetVertices(vertexMatching) &&
                pathCountCorrect(sourceGraph, edgeMatching) &&
                directedEdgesHavePaths(sourceGraph, vertexMatching, edgeMatching) &&
                undirectedEdgesHavePaths(sourceGraph, vertexMatching, edgeMatching) &&
                pathsAreNodeDisjoint(edgeMatching) &&
                noVertexMatchUsedInPath(vertexMatching, edgeMatching);
    }

    private static boolean noVertexMatchUsedInPath(@NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
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

    private static boolean pathsAreNodeDisjoint(@NotNull EdgeMatching edgeMatching) {
        for (Path path : edgeMatching.allPaths()) {
            List<Integer> intermediate = path.intermediate();
            if (!edgeMatching.allPaths().stream().allMatch(x -> x == path || x.intermediate().stream().noneMatch(intermediate::contains))) {
                return false;
            }
        }
        return true;
    }

    private static boolean undirectedEdgesHavePaths(@NotNull MyGraph sourceGraph, @NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
        if (sourceGraph.isDirected()) {
            return true;
        }
        for (MyEdge edge : sourceGraph.edgeSet()) {
            int edgeSourceTarget = vertexMatching.getPlacement().get(sourceGraph.getEdgeSource(edge));
            int edgeTargetTarget = vertexMatching.getPlacement().get(sourceGraph.getEdgeTarget(edge));
            long matches = edgeMatching.allPaths().stream().filter(x -> Set.of(x.last(), x.first()).equals(Set.of(edgeSourceTarget, edgeTargetTarget))).count();
            if (matches != 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean directedEdgesHavePaths(@NotNull MyGraph sourceGraph, @NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
        if (!sourceGraph.isDirected()) {
            return true;
        }
        for (MyEdge edge : sourceGraph.edgeSet()) {
            int edgeSourceTarget = vertexMatching.getPlacement().get(sourceGraph.getEdgeSource(edge));
            int edgeTargetTarget = vertexMatching.getPlacement().get(sourceGraph.getEdgeTarget(edge));
            long matches = edgeMatching.allPaths().stream().filter(x -> x.last() == edgeTargetTarget && x.first() == edgeSourceTarget).count();
            if (matches != 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean pathCountCorrect(@NotNull MyGraph sourceGraph, @NotNull EdgeMatching edgeMatching) {
        return edgeMatching.allPaths().size() == sourceGraph.edgeSet().size();
    }

    private static boolean uniqueTargetVertices(@NotNull VertexMatching vertexMatching) {
        return vertexMatching.getPlacement().size() == new TIntHashSet(vertexMatching.getPlacement()).size();
    }

    private static boolean everyVertexPlaced(@NotNull MyGraph sourceGraph, @NotNull VertexMatching vertexMatching) {
        return vertexMatching.getPlacement().size() >= sourceGraph.vertexSet().size();
    }
}
