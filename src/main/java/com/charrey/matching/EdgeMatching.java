package com.charrey.matching;

import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.router.PathIterator;
import com.charrey.util.UtilityData;

import java.util.*;

public class EdgeMatching extends VertexBlocker {

    private final VertexMatching vertexMatching;
    private final GraphGeneration source;
    private final Map<Vertex, Map<Vertex, PathIterator>> pathfinders;
    //private final Map<Vertex, Set<Path>> currentPathOccupation = new HashMap<>();

    private Vertex[][] edges; //do not change
    public LinkedList<LinkedList<Path>> paths; //change this
    
    private final UtilityData data;


    public EdgeMatching(VertexMatching vertexMatching, UtilityData data, GraphGeneration source, GraphGeneration target) {
        this.vertexMatching = vertexMatching;
        this.source = source;
        initPathsEdges(data);
        pathfinders = initPathFinders(data);
        this.data = data;
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
        EdgeMatching em = this;
        this.vertexMatching.setOnDeletion(vMatching -> em.synchronize());
        //target.getGraph().vertexSet().forEach(x -> currentPathOccupation.put(x, new HashSet<>()));
    }

    private Map<Vertex, Map<Vertex, PathIterator>> initPathFinders(UtilityData data) {
        Map<Vertex, Map<Vertex, PathIterator>> pathfinders = new HashMap<>();
        List<Vertex> allValues = data.getCompatibleValues();
        for (int i = 0; i < allValues.size() - 1; i++) {
            Vertex a = allValues.get(i);
            pathfinders.put(a, new HashMap<>());
            for (int j = i+1; j < allValues.size(); j++) {
                Vertex b = allValues.get(j);
                assert a.intData() < b.intData();
                pathfinders.get(a).put(b, new PathIterator(data.getTargetNeighbours(), a, b));
            }
        }
        while (paths.size() < data.getOrder().size()) {
            paths.add(new LinkedList<>());
        }
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));

        return pathfinders;

    }

    public boolean hasUnmatched() {
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        if (lastPlacedIndex == -1) {
            return false;
        }
        return paths.get(lastPlacedIndex).size() < edges[lastPlacedIndex].length;
    }

    public boolean canRetry() {
        if (vertexMatching.getPlacement().isEmpty()) {
            return false;
        }
        LinkedList<Path> pathList = paths.get(vertexMatching.getPlacement().size()-1);
        if (pathList.isEmpty()) {
            return false;
        }
        Path toRetry = pathList.getLast();
        Vertex a = toRetry.tail();
        Vertex b = toRetry.head();
        assert a.intData() < b.intData();
        if (!pathfinders.containsKey(a) || !pathfinders.get(a).containsKey(b)) {
            return false;
        }
        return pathfinders.get(a).get(b).hasNext(); //returns whether some next path exists between these two
    }

    public void retry() {
//        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
//        paths.get(vertexMatching.getPlacement().size() - 1).removeLast();
        LinkedList<Path> pathList = paths.get(vertexMatching.getPlacement().size()-1);
        assert !pathList.isEmpty();
        Path toRetry = pathList.getLast();
        //toRetry.intermediate().forEach(x -> currentPathOccupation.get(x).remove(toRetry));
        Vertex a = toRetry.tail();
        Vertex b = toRetry.head();
        assert a.intData() < b.intData();
        assert pathfinders.containsKey(a) && pathfinders.get(a).containsKey(b);
        Path path = pathfinders.get(a).get(b).next();
        assert !path.isEmpty();
        //path.intermediate().forEach(x -> currentPathOccupation.get(x).add(path));
        pathList.set(pathList.size() - 1, new Path(path));
    }

    public Path placeNextUnmatched() {
        assert this.hasUnmatched();
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        Vertex from = vertexMatching.getPlacement().get(edges[lastPlacedIndex][paths.get(lastPlacedIndex).size()].intData());
        Vertex to = vertexMatching.getPlacement().get(lastPlacedIndex);
        Vertex a = from.intData() < to.intData() ? from : to;
        Vertex b = a == from ? to : from;
        assert a.intData() < b.intData();
        if (!pathfinders.containsKey(a) || !pathfinders.get(a).containsKey(b)) {
            return null;
        }
        PathIterator iterator = pathfinders.get(a).get(b);
        if (iterator.hasNext()) {
            Path toReturn = iterator.next();
            assert !toReturn.isEmpty();
            addPath(toReturn);
            return toReturn;
        } else {
            return null;
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
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));

    }

    private void addPath(Path found) {
        assert !found.isEmpty();
        assert found.head().intData() > found.tail().intData();
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        Path added = new Path(found);
        //found.intermediate().forEach(x -> this.currentPathOccupation.get(x).add(added));
        paths.get(lastPlacedIndex).add(added);
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EdgeMatching{\n");
        for (List<Path> pathAddition : paths) {
            sb.append("\t").append(pathAddition).append("\n");
        }
        sb.append("}\n");
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
        return sb.toString();
    }

    public void synchronize() {
        for (int i = vertexMatching.getPlacement().size(); i < paths.size(); i++) {
            paths.get(i).clear();
        }
        for (Map.Entry<Vertex, Map<Vertex, PathIterator>> entry : pathfinders.entrySet()) {
            for (Map.Entry<Vertex, PathIterator> secondEntry : entry.getValue().entrySet()) {
                if (paths.stream().noneMatch(x -> x.stream().anyMatch(y -> y.tail() == entry.getKey() && y.head() == secondEntry.getKey()))) {
                    pathfinders.get(entry.getKey()).put(secondEntry.getKey(), new PathIterator(data.getTargetNeighbours(), entry.getKey(), secondEntry.getKey()));
                }
            }
        }
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));

    }

    public Set<Path> allPaths() {
        Set<Path> res = new HashSet<>();
        for (List<Path> pathList : paths) {
            res.addAll(pathList);
        }
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
        return Collections.unmodifiableSet(res);
    }


    public void removeLastPath() {
        Path removed = this.paths.get(this.vertexMatching.getPlacement().size() - 1).removeLast();
        assert removed.head().intData() > removed.tail().intData();
        Vertex a = removed.tail();
        Vertex b = removed.head();
        //this.pathfinders.get(a).put(b, new PathIterator(data.getTargetNeighbours(), a, b));
    }
}
