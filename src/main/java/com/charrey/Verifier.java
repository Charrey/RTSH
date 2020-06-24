package com.charrey;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Verifier {

    static boolean isCorrect(@NotNull MyGraph pattern, @NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
        //all nodes are placed
        if (vertexMatching.getPlacementUnsafe().size() < pattern.vertexSet().size()) {
            assert false;
            return false;
        }
        //all nodes are distinct
        if (vertexMatching.getPlacementUnsafe().size() != new HashSet<>(vertexMatching.getPlacementUnsafe()).size()) {
            assert false;
            return false;
        }

        //all edges are placed
        if (edgeMatching.allPaths().size() != pattern.edgeSet().size()) {
            assert false;
            return false;
        }
        if (pattern.isDirected()) {
            for (DefaultEdge edge : pattern.edgeSet()) {
                int edgeSourceTarget = vertexMatching.getPlacementUnsafe().get(pattern.getEdgeSource(edge));
                int edgeTargetTarget = vertexMatching.getPlacementUnsafe().get(pattern.getEdgeTarget(edge));
                long matches = edgeMatching.allPaths().stream().filter(x -> x.last() == edgeTargetTarget && x.first() == edgeSourceTarget).count();
                if (matches != 1) {
                    assert false;
                    return false;
                }
            }
        } else {
            for (DefaultEdge edge : pattern.edgeSet()) {
                int edgeSourceTarget = vertexMatching.getPlacementUnsafe().get(pattern.getEdgeSource(edge));
                int edgeTargetTarget = vertexMatching.getPlacementUnsafe().get(pattern.getEdgeTarget(edge));
                long matches = edgeMatching.allPaths().stream().filter(x -> Set.of(x.last(), x.first()).equals(Set.of(edgeSourceTarget, edgeTargetTarget))).count();
                if (matches != 1) {
                    assert false;
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
            if (vertexMatching.getPlacementUnsafe().stream().anyMatch(intermediate::contains)) {
                return false;
            }
        }
        return true;
    }
}
