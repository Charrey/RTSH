package com.charrey.matching;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.router.PathIterator;
import com.charrey.util.MultipleKeyMap;
import com.charrey.util.UtilityData;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EdgeMatching extends VertexBlocker {

    private final VertexMatching vertexMatching;
    private final GraphGeneration source;
    private MultipleKeyMap<Vertex, Vertex, PathIterator> pathfinders;
    private final Occupation occupation;
    //private final Map<Vertex, Set<Path>> currentPathOccupation = new HashMap<>();

    private Vertex[][] edges; //do not change
    public LinkedList<CopyOnWriteArrayList<Path>> paths; //change this
    
    private final UtilityData data;


    public EdgeMatching(VertexMatching vertexMatching, UtilityData data, GraphGeneration source, GraphGeneration target, Occupation occupation) {
        this.vertexMatching = vertexMatching;
        this.source = source;
        initPathsEdges(data);
        initPathFinders(data);
        this.data = data;
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
        EdgeMatching em = this;
        this.vertexMatching.setOnDeletion(em::synchronize);
        //target.getGraph().vertexSet().forEach(x -> currentPathOccupation.put(x, new HashSet<>()));
        this.occupation = occupation;
    }

    private void initPathFinders(UtilityData data) {
        pathfinders = new MultipleKeyMap<>();
        while (paths.size() < data.getOrder().size()) {
            paths.add(new CopyOnWriteArrayList<>());
        }
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
    }

    public boolean hasUnmatched() {
        //assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
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
        CopyOnWriteArrayList<Path> pathList = paths.get(vertexMatching.getPlacement().size()-1);
        if (pathList.isEmpty()) {
            return false;
        }
        return pathList.stream().anyMatch(path -> {
            Vertex a = path.tail();
            Vertex b = path.head();
            assert a.intData() < b.intData();
            if (!pathfinders.containsKey(a, b)) {
                pathfinders.put(a, b, new PathIterator(data.getTargetNeighbours(), a, b, occupation));
            }
            return pathfinders.get(a, b).hasNext();
        });
    }

    public void retry() {
//        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
//        paths.get(vertexMatching.getPlacement().size() - 1).removeLast();
        CopyOnWriteArrayList<Path> pathList = paths.get(vertexMatching.getPlacement().size()-1);
        assert !pathList.isEmpty();


        for (int i = pathList.size() - 1; i >= 0; i--) {
            Path toRetry = pathList.get(i);
            Vertex a = toRetry.tail();
            Vertex b = toRetry.head();
            assert a.intData() < b.intData();
            assert pathfinders.containsKey(a, b);

            if (pathfinders.get(a, b).hasNext()) {
                pathList.get(pathList.size() - 1).intermediate().forEach(occupation::releaseRouting);
                pathList.set(pathList.size() - 1, new Path(pathfinders.get(a, b).next()));
                pathList.get(pathList.size() - 1).intermediate().forEach(occupation::occupyRouting);
                return;
            } else {
                pathfinders.remove(a, b);
                removeLastPath();
            }
        }
        assert false;
    }

    public Path placeNextUnmatched() {
        assert this.hasUnmatched();
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        Vertex from = vertexMatching.getPlacement().get(edges[lastPlacedIndex][paths.get(lastPlacedIndex).size()].intData());
        Vertex to = vertexMatching.getPlacement().get(lastPlacedIndex);
        Vertex a = from.intData() < to.intData() ? from : to;
        Vertex b = a == from ? to : from;
        assert a.intData() < b.intData();
        if (!pathfinders.containsKey(a, b)) {
            pathfinders.put(a, b, new PathIterator(data.getTargetNeighbours(), a, b, occupation));
        }
        PathIterator iterator = pathfinders.get(a, b);
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
            paths.add(new CopyOnWriteArrayList<>());
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
        added.intermediate().forEach(occupation::occupyRouting);
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

    public void synchronize(Vertex vertex) {
        paths.get(vertexMatching.getPlacement().size()).forEach(x -> x.intermediate().forEach(occupation::releaseRouting));
        paths.get(vertexMatching.getPlacement().size()).clear();
        Set<Path> allPaths = allPaths();
        for (MultipleKeyMap.MultipleKeyMapEntry<Vertex, Vertex, PathIterator> entry : pathfinders.entrySet()) {
            if (allPaths.stream().noneMatch(y -> y.tail() == entry.getFirstKey() && y.head() == entry.getSecondKey())) {
                pathfinders.remove(entry.getFirstKey(), entry.getSecondKey());
            }
        }
    }

    public Set<Path> allPaths() {
        Set<Path> res = new HashSet<>();
        for (List<Path> pathList : paths) {
            res.addAll(pathList);
        }
        return Collections.unmodifiableSet(res);
    }

    public void removeLastPath() {
        List<Path> pathList = this.paths.get(this.vertexMatching.getPlacement().size() - 1);
        Path removed = pathList.remove(pathList.size() - 1);
        removed.intermediate().forEach(occupation::releaseRouting);
        assert removed.head().intData() > removed.tail().intData();
    }
}
