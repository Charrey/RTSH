package com.charrey.util;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import org.apache.commons.math3.random.RandomGenerator;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class Util {

    public static  <V> V pickRandom(Collection<V> collection, RandomGenerator random) {
        List<V> list = new LinkedList<>(collection);
        return list.get(random.nextInt(list.size()));
    }

    public static boolean isCorrect(MyGraph pattern, VertexMatching vertexMatching, EdgeMatching edgeMatching) {
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
                Vertex edgeSourceTarget = vertexMatching.getPlacementUnsafe().get(pattern.getEdgeSource(edge).data());
                Vertex edgeTargetTarget = vertexMatching.getPlacementUnsafe().get(pattern.getEdgeTarget(edge).data());
                long matches = edgeMatching.allPaths().stream().filter(x -> x.head().equals(edgeTargetTarget) && x.tail().equals(edgeSourceTarget)).count();
                if (matches != 1) {
                    assert false;
                    return false;
                }
            }
        } else {
            for (DefaultEdge edge : pattern.edgeSet()) {
                Vertex edgeSourceTarget = vertexMatching.getPlacementUnsafe().get(pattern.getEdgeSource(edge).data());
                Vertex edgeTargetTarget = vertexMatching.getPlacementUnsafe().get(pattern.getEdgeTarget(edge).data());
                long matches = edgeMatching.allPaths().stream().filter(x -> Set.of(x.head(), x.tail()).equals(Set.of(edgeSourceTarget, edgeTargetTarget))).count();
                if (matches != 1) {
                    assert false;
                    return false;
                }
            }
        }

        //the intermediate list of nodes are distinct
        for (Path path : edgeMatching.allPaths()) {
            List<Vertex> intermediate = path.intermediate();
            if (!edgeMatching.allPaths().stream().allMatch(x -> x == path || x.intermediate().stream().noneMatch(intermediate::contains))) {
                return false;
            }
        }

        //the intermediate list of nodes are disjoint from the nodes
        for (Path path : edgeMatching.allPaths()) {
            List<Vertex> intermediate = path.intermediate();
            if (vertexMatching.getPlacementUnsafe().stream().anyMatch(intermediate::contains)) {
                return false;
            }
        }
        return true;
    }
}
