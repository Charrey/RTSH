package com.charrey.matching;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.router.PathIterator;
import com.charrey.util.IndexMap;
import com.charrey.util.MultipleKeyMap;
import com.charrey.util.UtilityData;

import java.util.*;

public class EdgeMatching extends VertexBlocker {

    private final VertexMatching vertexMatching;
    private final GraphGeneration source;
    private final int domainSize;

    private MultipleKeyMap<PathIterator> pathfinders;
    private final Occupation occupation;

    private Vertex[][] edges; //do not change
    public LinkedList<LinkedList<Path>> paths;

    private final Map<Vertex, List<PathIterator>> headMap;
    private final Map<Vertex, List<PathIterator>> tailMap;
    
    private final UtilityData data;

    public EdgeMatching(VertexMatching vertexMatching, UtilityData data, GraphGeneration source, GraphGeneration target, Occupation occupation) {
        this.vertexMatching = vertexMatching;
        this.source = source;
        initPathsEdges(data);
        initPathFinders(target.getGraph().vertexSet().size(), data);
        this.data = data;
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
        EdgeMatching em = this;
        this.vertexMatching.setOnDeletion(em::synchronize);
        this.occupation = occupation;
        headMap = new IndexMap<>(target.getGraph().vertexSet().size());
        tailMap = new IndexMap<>(target.getGraph().vertexSet().size());
        for (Vertex v: target.getGraph().vertexSet()) {
            headMap.put(v, new LinkedList<>());
            tailMap.put(v, new LinkedList<>());
        }
        this.domainSize = target.getGraph().vertexSet().size();
    }

    private void initPathFinders(int targetSize, UtilityData data) {
        pathfinders = new MultipleKeyMap<>(targetSize, targetSize);
        while (paths.size() < data.getOrder().size()) {
            paths.add(new LinkedList<>());
        }
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
    }

    public boolean hasUnmatched() {
        int lastPlacedIndex = vertexMatching.getPlacementUnsafe().size() - 1;
        if (lastPlacedIndex == -1) {
            return false;
        }
        return paths.get(lastPlacedIndex).size() < edges[lastPlacedIndex].length;
    }

    public boolean retry() {
        if (vertexMatching.getPlacementUnsafe().isEmpty()) {
            return false;
        }
        List<Path> pathList = paths.get(vertexMatching.getPlacementUnsafe().size()-1);
        if (pathList.isEmpty()) {
            return false;
        }
        for (int i = pathList.size() - 1; i >= 0; i--) {
            Path toRetry = pathList.get(i);
            Vertex tail = toRetry.tail();
            Vertex head = toRetry.head();
            assert tail.data() < head.data();
            assert pathfinders.containsKey(tail, head);
            pathList.get(pathList.size() - 1).intermediate().forEach(occupation::releaseRouting);
            PathIterator pathfinder = pathfinders.get(tail, head);
            Path pathFound = pathfinder.next();
            if (pathFound != null) {
                Path toAdd = new Path(pathFound);
                pathList.set(pathList.size() - 1, toAdd);
                pathList.get(pathList.size() - 1).intermediate().forEach(occupation::occupyRouting);
                return true;
            } else {
                pathList.get(pathList.size() - 1).intermediate().forEach(occupation::occupyRouting);
                pathfinders.remove(tail, head);
                headMap.get(head).remove(pathfinder);
                tailMap.get(tail).remove(pathfinder);
                removeLastPath();
            }

        }
        return false;
    }

    public Path placeNextUnmatched() {
        assert this.hasUnmatched();
        int lastPlacedIndex = vertexMatching.getPlacementUnsafe().size() - 1;
        Vertex from = vertexMatching.getPlacementUnsafe().get(edges[lastPlacedIndex][paths.get(lastPlacedIndex).size()].data());
        Vertex to = vertexMatching.getPlacementUnsafe().get(lastPlacedIndex);
        Vertex tail = from.data() < to.data() ? from : to;
        Vertex head = tail == from ? to : from;
        assert tail.data() < head.data();
        if (!pathfinders.containsKey(tail, head)) {
            PathIterator toAdd = new PathIterator(domainSize, data.getTargetNeighbours(), tail, head, occupation);
            assert headMap.containsKey(head);
            headMap.get(head).add(toAdd);
            tailMap.get(tail).add(toAdd);
            pathfinders.put(tail, head, toAdd);
        }
        PathIterator iterator = pathfinders.get(tail, head);
        Path toReturn = iterator.next();
        if (toReturn != null) {
            addPath(toReturn);
        }
        return toReturn;

    }

    private void initPathsEdges(UtilityData data) {
        edges = new Vertex[source.getGraph().vertexSet().size()][];
        paths = new LinkedList<>();
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
        assert found.head().data() > found.tail().data();
        int lastPlacedIndex = vertexMatching.getPlacementUnsafe().size() - 1;
        Path added = new Path(found);
        paths.get(lastPlacedIndex).add(added);
        added.intermediate().forEach(occupation::occupyRouting);

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
        assert !vertexMatching.getPlacementUnsafe().contains(vertex);
        paths.get(vertexMatching.getPlacementUnsafe().size()).forEach(x -> x.intermediate().forEach(occupation::releaseRouting));
        paths.get(vertexMatching.getPlacementUnsafe().size()).clear();
        for (Iterator<PathIterator> i = headMap.get(vertex).iterator(); i.hasNext();) {
            PathIterator pathIt = i.next();
            pathfinders.removeIfPresent(pathIt.tail(), pathIt.head());
            i.remove();
        }
        for (Iterator<PathIterator> i = tailMap.get(vertex).iterator(); i.hasNext();) {
            PathIterator pathIt = i.next();
            pathfinders.removeIfPresent(pathIt.tail(), pathIt.head());
            i.remove();
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
        List<Path> pathList = this.paths.get(this.vertexMatching.getPlacementUnsafe().size() - 1);
        Path removed = pathList.remove(pathList.size() - 1);
        removed.intermediate().forEach(occupation::releaseRouting);
        assert removed.head().data() > removed.tail().data();
    }


}
