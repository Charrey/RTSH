package com.charrey.util;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import org.apache.commons.math3.random.RandomGenerator;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Util {
    private static final Random random = new Random();

    public static  <V> V pickRandom(Collection<V> collection, RandomGenerator random) {
        List<V> list = new LinkedList<>(collection);
        return list.get(random.nextInt(list.size()));
    }

    public static void appendToFile(String file, String valueOf) throws IOException {
        try (FileWriter writer = new FileWriter(Paths.get(file).toRealPath().toFile(), true)) {
            writer.write(valueOf + "\n");
        }
    }

    public static boolean isCorrect(Graph<Vertex, DefaultEdge> pattern, Graph<Vertex, DefaultEdge> target, VertexMatching vertexMatching, EdgeMatching edgeMatching) {
        //all nodes are placed
        if (vertexMatching.getPlacement().size() < pattern.vertexSet().size()) {
            return false;
        }
        //all nodes are distinct
        if (vertexMatching.getPlacement().size() != new HashSet<>(vertexMatching.getPlacement()).size()) {
            return false;
        }

        //all edges are placed
        if (edgeMatching.allPaths().size() != pattern.edgeSet().size()) {
            return false;
        }
        for (DefaultEdge edge : pattern.edgeSet()) {
            Vertex edgeSourceTarget = vertexMatching.getPlacement().get(pattern.getEdgeSource(edge).intData());
            Vertex edgeTargetTarget = vertexMatching.getPlacement().get(pattern.getEdgeTarget(edge).intData());
            long matches = edgeMatching.allPaths().stream().filter(x -> Set.of(x.head(), x.tail()).equals(Set.of(edgeSourceTarget, edgeTargetTarget))).count();
            if (matches != 1) {
                return false;
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
            if (vertexMatching.getPlacement().stream().anyMatch(intermediate::contains)) {
                return false;
            }
        }
        return true;
    }
}
