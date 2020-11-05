package com.charrey;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.util.Util;
import com.charrey.util.datastructures.MultipleKeyMap;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

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
        List<Integer> vertexPlacement = vertexMatching.get();
        boolean everyVertexPlaced = everyVertexPlaced(sourceGraph, vertexMatching);
        boolean uniqueTargetVertices = uniqueTargetVertices(vertexMatching);
        boolean pathCountCorrect = pathCountCorrect(sourceGraph, edgeMatching);
        boolean directedEdgesHavePaths = directedEdgesHavePaths(sourceGraph, vertexPlacement, edgeMatching);
        boolean undirectedEdgesHavePaths = undirectedEdgesHavePaths(sourceGraph, vertexPlacement, edgeMatching);
        boolean pathsAreNodeDisjoint = pathsAreNodeDisjoint(edgeMatching);
        boolean noVertexMatchUsedInPath = noVertexMatchUsedInPath(vertexPlacement, edgeMatching);
        return everyVertexPlaced &&
                uniqueTargetVertices &&
                pathCountCorrect &&
                directedEdgesHavePaths &&
                undirectedEdgesHavePaths &&
                pathsAreNodeDisjoint &&
                noVertexMatchUsedInPath;
    }

    private static boolean noVertexMatchUsedInPath(@NotNull List<Integer> vertexMatching, @NotNull EdgeMatching edgeMatching) {
        //the intermediate list of nodes are disjoint from the nodes
        for (Path path : edgeMatching.allPaths()) {
            Path intermediate = path.intermediate();
            if (vertexMatching.stream().anyMatch(intermediate::contains)) {
                return false;
            }
        }
        return true;
    }

    private static boolean pathsAreNodeDisjoint(@NotNull EdgeMatching edgeMatching) {
        for (Path path : edgeMatching.allPaths()) {
            Path intermediate = path.intermediate();
            if (!edgeMatching.allPaths().stream().allMatch(x -> x == path || x.intermediate().noneMatch(intermediate::contains))) {
                return false;
            }
        }
        return true;
    }

    private static boolean undirectedEdgesHavePaths(@NotNull MyGraph sourceGraph, @NotNull List<Integer> vertexMatching, @NotNull EdgeMatching edgeMatching) {
        if (sourceGraph.isDirected()) {
            return true;
        }
        MultipleKeyMap<Integer> needed = new MultipleKeyMap<>();
        sourceGraph.edgeSet().forEach(myEdge -> {
            if (!needed.containsKey(myEdge.getSource(), myEdge.getTarget())) {
                needed.put(myEdge.getSource(), myEdge.getTarget(), 1);
            } else {
                needed.put(myEdge.getSource(), myEdge.getTarget(), needed.get(myEdge.getSource(), myEdge.getTarget()) + 1);
            }
        });
        final boolean[] toReturn = {true};
        needed.entrySet().forEach(entry -> {
            int edgeSourceTarget = vertexMatching.get(entry.getFirstKey());
            int edgeTargetTarget = vertexMatching.get(entry.getSecondKey());
            long matches = edgeMatching.allPaths().stream().filter(x -> new HashSet<>(Util.listOf(x.last(), x.first())).equals(new HashSet<>(Util.listOf(edgeSourceTarget, edgeTargetTarget)))).count();
            if (matches != entry.getValue()) {
                toReturn[0] = false;
            }
        });
        return toReturn[0];
    }

    private static boolean directedEdgesHavePaths(@NotNull MyGraph sourceGraph, @NotNull List<Integer> vertexMatching, @NotNull EdgeMatching edgeMatching) {
        if (!sourceGraph.isDirected()) {
            return true;
        }
        for (MyEdge edge : sourceGraph.edgeSet()) {
            int edgeSourceTarget = vertexMatching.get(sourceGraph.getEdgeSource(edge));
            int edgeTargetTarget = vertexMatching.get(sourceGraph.getEdgeTarget(edge));
            long matches = edgeMatching.allPaths().stream().filter(x -> x.last() == edgeTargetTarget && x.first() == edgeSourceTarget).count();
            if (matches == 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean pathCountCorrect(@NotNull MyGraph sourceGraph, @NotNull EdgeMatching edgeMatching) {
        return edgeMatching.allPaths().size() == sourceGraph.edgeSet().size();
    }

    private static boolean uniqueTargetVertices(@NotNull VertexMatching vertexMatching) {
        return vertexMatching.size() == new TIntHashSet(vertexMatching.get()).size();
    }

    private static boolean everyVertexPlaced(@NotNull MyGraph sourceGraph, @NotNull VertexMatching vertexMatching) {
        return vertexMatching.size() >= sourceGraph.vertexSet().size();
    }
}
