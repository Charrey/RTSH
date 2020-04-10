package com.charrey.matching;

import com.charrey.example.GraphGenerator;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.matching.matchResults.edge.EdgeMatchResult;
import com.charrey.matching.matchResults.edge.SuccessPathResult;
import com.charrey.router.PathIterator;
import com.charrey.util.UtilityData;

import java.util.*;

public class EdgeMatching extends VertexBlocker {

    private final VertexMatching vertexMatching;
    private final GraphGenerator.GraphGeneration source;
    private final Map<Vertex, Map<Vertex, PathIterator>> pathfinders;
    private final GraphGenerator.GraphGeneration target;

    private Vertex[][] edges; //do not change
    private LinkedList<List<Path>> paths; //change this
    
    private final UtilityData data;


    public EdgeMatching(VertexMatching vertexMatching, UtilityData data, GraphGenerator.GraphGeneration source, GraphGenerator.GraphGeneration target) {
        this.vertexMatching = vertexMatching;
        this.source = source;
        initPathsEdges(data);
        pathfinders = initPathFinders(target, data);
        this.data = data;
        this.target = target;
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
        while (paths.size() < vertexMatching.getOrder().size()) {
            paths.add(new LinkedList<>());
        }
        return res;
    }

    public boolean hasUnmatched() {
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        if (lastPlacedIndex == -1) {
            return false;
        }
        return paths.get(lastPlacedIndex).size() < edges[lastPlacedIndex].length;
    }

    public EdgeMatchResult tryNext() {
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        Vertex from = vertexMatching.getPlacement().get(edges[lastPlacedIndex][paths.get(lastPlacedIndex).size()].intData());
        Vertex to = vertexMatching.getPlacement().get(lastPlacedIndex);
        Vertex a = from.intData() < to.intData() ? from : to;
        Vertex b = a == from ? to : from;
        if (!pathfinders.containsKey(a) || !pathfinders.get(a).containsKey(b)) {
            return new SuccessPathResult(null);
        }
        PathIterator iterator = pathfinders.get(a).get(b);
        if (iterator.hasNext()) {
            SuccessPathResult toReturn = new SuccessPathResult(iterator.next());
            update(toReturn);
            return toReturn;
        } else {
            return new SuccessPathResult(null);
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
        final StringBuilder sb = new StringBuilder("EdgeMatching{\n");
        for (List<Path> pathAddition : paths) {
            sb.append("\t").append(pathAddition).append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public boolean blocksNonRecursive(Vertex v) {
        return paths.stream().anyMatch(y -> y.stream().anyMatch(z -> z.intermediate().contains(v)));
    }

    public void synchronize() {
        for (int i = vertexMatching.getPlacement().size(); i < paths.size(); i++) {
            for (Path j : paths.get(i)) {
                pathfinders.get(j.head()).put(j.tail(), new PathIterator(target, data.getTargetNeighbours(), j.head(), j.tail()));
                pathfinders.get(j.tail()).put(j.head(), new PathIterator(target, data.getTargetNeighbours(), j.tail(), j.head()));
            }
            paths.get(i).clear();
        }
    }

    public void removePath(Path removed) {
        for (List<Path> list : paths) {
            list.remove(removed);
        }
    }
}
