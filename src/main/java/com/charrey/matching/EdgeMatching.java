package com.charrey.matching;

import com.charrey.example.GraphGenerator;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.matching.matchResults.edge.EdgeMatchResult;
import com.charrey.router.PathIterator;
import com.charrey.util.UtilityData;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.function.BinaryOperator;

public class EdgeMatching {

    private final VertexMatching vertexMatching;
    private final GraphGenerator.GraphGeneration source;
    private final Map<Vertex, Map<Vertex, PathIterator>> pathfinders;

    private Vertex[][] edges; //do not change
    private List<List<Path>> paths; //change this
    
    private final UtilityData data;


    public EdgeMatching(VertexMatching vertexMatching, UtilityData data, GraphGenerator.GraphGeneration source, GraphGenerator.GraphGeneration target) {
        this.vertexMatching = vertexMatching;
        this.source = source;
        initPathsEdges(data);
        pathfinders = initPathFinders(target, data);
        this.data = data;
    }

    private Map<Vertex, Map<Vertex, PathIterator>> initPathFinders(GraphGenerator.GraphGeneration targetGraph, UtilityData data) {
        Map<Vertex, Map<Vertex, PathIterator>> res = new HashMap<>();
        List<Vertex> allValues = new LinkedList<>(data.getCompatibility().values()
                .stream()
                .reduce(new HashSet<>(), (vertices, vertices2) -> {
                    vertices.addAll(vertices2);
                    return vertices;
                }));
        allValues.sort(Comparator.comparingInt(Vertex::intData));
        for (int i = 0; i < allValues.size() - 1; i++) {
            Vertex a = allValues.get(i);
            res.put(a, new HashMap<>());
            for (int j = i+1; j < allValues.size(); j++) {
                Vertex b = allValues.get(j);
                res.get(a).put(b, new PathIterator(targetGraph, data.getTargetNeighbours(), a, b));
            }
        }
        return res;
    }

    public boolean hasUnmatched() {
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        if (lastPlacedIndex == -1) {
            return false;
        }
        while (paths.size() < edges[lastPlacedIndex].length) {
            paths.add(new LinkedList<>());
        }
        return paths.get(lastPlacedIndex).size() < edges[lastPlacedIndex].length;
    }

    public EdgeMatchResult tryNext() {
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        while (paths.size() < edges[lastPlacedIndex].length) {
            paths.add(new LinkedList<>());
        }
        Vertex from = vertexMatching.getPlacement().get(edges[lastPlacedIndex][paths.get(lastPlacedIndex).size()].intData());
        Vertex to = vertexMatching.getPlacement().get(lastPlacedIndex);
        Vertex a = from.intData() < to.intData() ? from : to;
        Vertex b = a == from ? to : from;
        PathIterator iterator = pathfinders.get(a).get(b);
        if (iterator.hasNext()) {
            return new SuccessPathResult(iterator.next());
        } else {
            return new NoMorePathsResult();
        }
    }


    private void initPathsEdges(UtilityData data) {
        edges = new Vertex[source.getGraph().vertexSet().size()][];
        paths = new LinkedList<>();//[edges.length];
        for (int i = 0; i < data.getOrder().size(); i++) {
            int tempi = i;
            edges[i] = data.getOrder().subList(0, i)
                    .stream()
                    .filter(x -> source.getGraph().getEdge(x, data.getOrder().get(tempi)) != null)
                    .toArray(Vertex[]::new);
            paths.add(new LinkedList<>());
        }
    }

    public void update(SuccessPathResult matchResult) {
        Path found = matchResult.getPath();
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        paths.get(lastPlacedIndex).add(found);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EdgeMatching{");
        for (List<Path> pathAddition : paths) {
            sb.append(pathAddition).append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
