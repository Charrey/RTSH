package com.charrey.matching;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.router.PathIterator;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.LinkedIndexSet;
import com.charrey.util.datastructures.MultipleKeyMap;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.lang.reflect.Array;
import java.util.*;

public class EdgeMatching extends VertexBlocker {

    private final VertexMatching vertexMatching;
    private final GraphGeneration source;
    private final Graph<Vertex, DefaultEdge> targetGraph;

    private MultipleKeyMap<PathIterator> pathfinders;
    private final Occupation occupation;

    private Vertex[][] edges; //do not change
    public ArrayList<LinkedList<Path>> paths;

    private final LinkedIndexSet<PathIterator>[] headMap2;
    private final LinkedIndexSet<PathIterator>[] tailMap2;
    private final UtilityData data;

    @SuppressWarnings("unchecked")
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
        this.targetGraph = target.getGraph();
        headMap2 = (LinkedIndexSet<PathIterator>[]) Array.newInstance(LinkedIndexSet.class, targetGraph.vertexSet().size());
        tailMap2 = (LinkedIndexSet<PathIterator>[]) Array.newInstance(LinkedIndexSet.class, targetGraph.vertexSet().size());
        for (Vertex v: target.getGraph().vertexSet()) {
            headMap2[v.data()] = new LinkedIndexSet<>(targetGraph.vertexSet().size() * (1 + targetGraph.vertexSet().size()), PathIterator.class);
            tailMap2[v.data()] = new LinkedIndexSet<>(targetGraph.vertexSet().size() * (1 + targetGraph.vertexSet().size()), PathIterator.class);
        }

    }

    private void initPathFinders(int targetSize, UtilityData data) {
        pathfinders = new MultipleKeyMap<>(targetSize, targetSize, PathIterator.class);
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
        int placementSize = vertexMatching.getPlacementUnsafe().size();
        assert occupation.domainChecker.checkOK(placementSize);
        for (int i = pathList.size() - 1; i >= 0; i--) {
            Path toRetry = pathList.get(i);
            Vertex tail = toRetry.tail();
            Vertex head = toRetry.head();
            assert tail.data() < head.data();
            assert pathfinders.containsKey(tail, head);
            PathIterator pathfinder = pathfinders.get(tail, head);
            Path pathFound = pathfinder.next();
            if (pathFound != null) {
                Path toAdd = new Path(pathFound);
                pathList.set(pathList.size() - 1, toAdd);
                assert occupation.domainChecker.checkOK(placementSize);
                return true;
            } else {
                pathfinders.remove(tail, head);
                headMap2[head.data()].remove(pathfinder);
                tailMap2[tail.data()].remove(pathfinder);
                removeLastPath();
            }
        }
        assert occupation.domainChecker.checkOK(placementSize);
        return false;
    }

    public Path placeNextUnmatched() {
        assert occupation.domainChecker.checkOK(vertexMatching.getPlacementUnsafe().size());
        assert this.hasUnmatched();
        //get things
        int lastPlacedIndex = vertexMatching.getPlacementUnsafe().size() - 1;
        Vertex from = vertexMatching.getPlacementUnsafe().get(edges[lastPlacedIndex][paths.get(lastPlacedIndex).size()].data());
        Vertex to = vertexMatching.getPlacementUnsafe().get(lastPlacedIndex);
        Vertex tail = from.data() < to.data() ? from : to;
        int tailData = tail.data();
        Vertex head = tail == from ? to : from;
        int headData = head.data();
        assert tail.data() < head.data();
        //get pathIterator
        if (!pathfinders.containsKey(tail, head)) {
            PathIterator toAdd = PathIterator.get(targetGraph, data, tail, head, occupation, () -> vertexMatching.getPlacementUnsafe().size());
            headMap2[headData].remove(toAdd);
            headMap2[headData].add(toAdd);
            tailMap2[tailData].remove(toAdd);
            tailMap2[tailData].add(toAdd);
            pathfinders.put(tail, head, toAdd);
        }
        PathIterator iterator = pathfinders.get(tail, head);
        Path toReturn = iterator.next();
        if (toReturn != null) {
            addPath(toReturn);
            assert occupation.domainChecker.checkOK(vertexMatching.getPlacementUnsafe().size());
            return toReturn;
        } else {
            iterator.reset();
            assert occupation.domainChecker.checkOK(vertexMatching.getPlacementUnsafe().size());
            return null;
        }
    }

    private void initPathsEdges(UtilityData data) {
        edges = new Vertex[source.getGraph().vertexSet().size()][];
        paths = new ArrayList<>(data.getOrder().size());
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
        assert occupation.domainChecker.checkOK(vertexMatching.getPlacementUnsafe().size());
        int vertexData = vertex.data();
        assert !vertexMatching.getPlacementUnsafe().contains(vertex);
        paths.get(vertexMatching.getPlacementUnsafe().size()).clear();
        for (Iterator<PathIterator> i = headMap2[vertexData].iterator(); i.hasNext();) {
            PathIterator pathIt = i.next();
            pathfinders.get(pathIt.tail(), pathIt.head()).reset();
            i.remove();
        }
        for (Iterator<PathIterator> i = tailMap2[vertexData].iterator(); i.hasNext();) {
            PathIterator pathIt = i.next();
            pathfinders.get(pathIt.tail(), pathIt.head()).reset();
            i.remove();
        }
        assert occupation.domainChecker.checkOK(vertexMatching.getPlacementUnsafe().size());
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
        //removed.intermediate().forEach(x -> occupation.releaseRouting(vertexMatching.getPlacementUnsafe().size(), x)); // this should be the empty path now
        assert removed.head().data() > removed.tail().data();
    }


}
