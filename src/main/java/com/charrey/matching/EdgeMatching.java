package com.charrey.matching;

import com.charrey.Occupation;
import com.charrey.exceptions.EmptyDomainException;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.router.PathIterator;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.LinkedIndexSet;
import com.charrey.util.datastructures.MultipleKeyMap;

import java.lang.reflect.Array;
import java.util.*;


public class EdgeMatching extends VertexBlocker {

    private final VertexMatching vertexMatching;
    private final GraphGeneration source;
    private final int domainSize;

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
        this.domainSize = target.getGraph().vertexSet().size();
        headMap2 = (LinkedIndexSet<PathIterator>[]) Array.newInstance(LinkedIndexSet.class, domainSize);
        tailMap2 = (LinkedIndexSet<PathIterator>[]) Array.newInstance(LinkedIndexSet.class, domainSize);
        for (Vertex v: target.getGraph().vertexSet()) {
            headMap2[v.data()] = new LinkedIndexSet<>(domainSize * (1 + domainSize), PathIterator.class);
            tailMap2[v.data()] = new LinkedIndexSet<>(domainSize * (1 + domainSize), PathIterator.class);
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
        for (int i = pathList.size() - 1; i >= 0; i--) {
            Path toRetry = pathList.get(i);
            Vertex tail = toRetry.tail();
            Vertex head = toRetry.head();
            assert tail.data() < head.data();
            assert pathfinders.containsKey(tail, head);
            int placementSize = vertexMatching.getPlacementUnsafe().size();

            Path previousPath = pathList.get(pathList.size() - 1);
            ArrayList<Vertex> previousOccupation = new ArrayList<Vertex>(previousPath.intermediate());
            previousOccupation.forEach(x -> occupation.releaseRouting(placementSize, x));
            PathIterator pathfinder = pathfinders.get(tail, head);
            Path pathFound = pathfinder.next();
            if (pathFound != null) {
                Path toAdd = new Path(pathFound);
                pathList.set(pathList.size() - 1, toAdd);
                try {
                    occupation.occupyRoutingAndCheck(placementSize, pathList.get(pathList.size() - 1).intermediate());
                } catch (EmptyDomainException e) {
                    try {
                        occupation.occupyRoutingAndCheck(placementSize, previousOccupation);
                    } catch (EmptyDomainException ignored) {
                        assert false;
                    }
                    pathList.set(pathList.size()-1, previousPath);
                    return retry();
                }
                return true;
            } else {
                try {
                    occupation.occupyRoutingAndCheck(vertexMatching.getPlacementUnsafe().size(), pathList.get(pathList.size() - 1).intermediate());
                } catch (EmptyDomainException ignored) {
                    assert false;
                }
                pathfinders.remove(tail, head);
                headMap2[head.data()].remove(pathfinder);
                tailMap2[tail.data()].remove(pathfinder);
                removeLastPath();
            }
        }
        return false;
    }

    public Path placeNextUnmatched() {
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
            PathIterator toAdd = new PathIterator(domainSize, data.getTargetNeighbours(), tail, head, occupation);
            headMap2[headData].remove(toAdd);
            headMap2[headData].add(toAdd);
            tailMap2[tailData].remove(toAdd);
            tailMap2[tailData].add(toAdd);
            pathfinders.put(tail, head, toAdd);
        }
        PathIterator iterator = pathfinders.get(tail, head);
        Path toReturn = null;
        try {
            toReturn = iterator.next();
        } catch (AssertionError e) {
            pathfinders.get(tail, head).reset();
        }
        if (toReturn != null) {
            try {
                addPath(toReturn);
            } catch (EmptyDomainException e) {
                return placeNextUnmatched();
            }
            return toReturn;
        }
        return null;

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

    private void addPath(Path found) throws EmptyDomainException {
        assert !found.isEmpty();
        assert found.head().data() > found.tail().data();
        int lastPlacedIndex = vertexMatching.getPlacementUnsafe().size() - 1;
        Path added = new Path(found);
        paths.get(lastPlacedIndex).add(added);
        try {
            occupation.occupyRoutingAndCheck(vertexMatching.getPlacementUnsafe().size(), added.intermediate());
        } catch (EmptyDomainException e) {
            paths.get(lastPlacedIndex).removeLast();
            throw e;
        }
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
        int vertexData = vertex.data();
        assert !vertexMatching.getPlacementUnsafe().contains(vertex);
        paths.get(vertexMatching.getPlacementUnsafe().size()).forEach(x -> x.intermediate().forEach(y -> occupation.releaseRouting(vertexMatching.getPlacementUnsafe().size(), y)));
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
        removed.intermediate().forEach(x -> occupation.releaseRouting(vertexMatching.getPlacementUnsafe().size(), x));
        assert removed.head().data() > removed.tail().data();
    }


}
