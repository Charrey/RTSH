package com.charrey.matching;

import com.charrey.Occupation;
import com.charrey.Stateable;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.PathIterator;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.LinkedIndexSet;
import com.charrey.util.datastructures.MultipleKeyMap;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class EdgeMatching extends VertexBlocker implements Stateable {

    private final VertexMatching vertexMatching;
    private final MyGraph source;
    private final MyGraph targetGraph;
    private final boolean directed;

    private MultipleKeyMap<PathIterator> pathfinders;
    private final Occupation occupation;

    private Vertex[][] edges; //do not change
    private boolean[][] incoming; //only for directed graphs

    public ArrayList<LinkedList<Path>> paths;

    private final LinkedIndexSet<PathIterator>[] headMap;
    private final LinkedIndexSet<PathIterator>[] tailMap;
    private final UtilityData data;

    @SuppressWarnings("unchecked")
    public EdgeMatching(VertexMatching vertexMatching, UtilityData data, MyGraph source, MyGraph target, Occupation occupation) {
        this.vertexMatching = vertexMatching;
        this.source = source;
        this.data = data;
        this.targetGraph = target;
        this.directed = target.isDirected();
        initPathsEdges();
        initPathFinders();
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));
        EdgeMatching em = this;
        this.vertexMatching.setOnDeletion(em::synchronize);
        this.occupation = occupation;
        headMap = (LinkedIndexSet<PathIterator>[]) Array.newInstance(LinkedIndexSet.class, targetGraph.vertexSet().size());
        tailMap = (LinkedIndexSet<PathIterator>[]) Array.newInstance(LinkedIndexSet.class, targetGraph.vertexSet().size());
        for (Vertex v: target.vertexSet()) {
            headMap[v.data()] = new LinkedIndexSet<>(targetGraph.vertexSet().size() * (1 + targetGraph.vertexSet().size()), PathIterator.class);
            tailMap[v.data()] = new LinkedIndexSet<>(targetGraph.vertexSet().size() * (1 + targetGraph.vertexSet().size()), PathIterator.class);
        }
    }

    private void initPathFinders() {
        int targetSize = targetGraph.vertexSet().size();
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
            assert directed || tail.data() < head.data();
            assert pathfinders.containsKey(tail, head);
            PathIterator pathfinder = pathfinders.get(tail, head);
            Path pathFound = pathfinder.next();
            if (pathFound != null) {
                assert pathFound.tail() == tail : "Expected: " + tail + ", actual: " + pathFound.tail();
                assert pathFound.head() == head : "Expected: " + head + ", actual: " + pathFound.head();
                Path toAdd = new Path(pathFound);
                pathList.set(pathList.size() - 1, toAdd);
                assert occupation.domainChecker.checkOK(placementSize);
                return true;
            } else {
                pathfinders.remove(tail, head);
                headMap[head.data()].remove(pathfinder);
                tailMap[tail.data()].remove(pathfinder);
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
        if (directed && !incoming[lastPlacedIndex][paths.get(lastPlacedIndex).size()]) {
            //swap
            Vertex temp  = to;
            to = from;
            from = temp;
        }
        Vertex tail = directed ? from : (from.data() < to.data() ? from : to);
        Vertex head = directed ? to : (tail == from ? to : from);
        int tailData = tail.data();
        int headData = head.data();
        assert directed || tail.data() < head.data();
        //get pathIterator
        if (!pathfinders.containsKey(tail, head)) {
            PathIterator toAdd = PathIterator.get(targetGraph, data, tail, head, occupation, () -> vertexMatching.getPlacementUnsafe().size());
            headMap[headData].remove(toAdd);
            headMap[headData].add(toAdd);
            tailMap[tailData].remove(toAdd);
            tailMap[tailData].add(toAdd);
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

    private void initPathsEdges() {
        edges = new Vertex[source.vertexSet().size()][];
        paths = new ArrayList<>(data.getOrder().size());
        incoming = new boolean[data.getOrder().size()][];
        for (int i = 0; i < data.getOrder().size(); i++) {
            int tempi = i;
            if (!directed) {
                edges[i] = data.getOrder().subList(0, i)
                        .stream()
                        .filter(x -> source.getEdge(x, data.getOrder().get(tempi)) != null)
                        .toArray(Vertex[]::new);
            } else {
                List<Vertex> incomingEdges = data.getOrder().subList(0, i)
                        .stream()
                        .filter(x -> source.getEdge(x, data.getOrder().get(tempi)) != null)
                        .collect(Collectors.toUnmodifiableList());
                List<Vertex> outgoingEdges = data.getOrder().subList(0, i)
                        .stream()
                        .filter(x -> source.getEdge(data.getOrder().get(tempi), x) != null)
                        .collect(Collectors.toUnmodifiableList());
                incoming[i] = new boolean[incomingEdges.size() + outgoingEdges.size()];
                edges[i] = new Vertex[incomingEdges.size() + outgoingEdges.size()];
                for (int j = 0; j < incomingEdges.size(); j++) {
                    incoming[i][j] = true;
                    edges[i][j] = incomingEdges.get(j);
                }
                for (int j = 0; j < outgoingEdges.size(); j++) {
                    edges[i][j + incomingEdges.size()] = outgoingEdges.get(j);
                }
            }
            paths.add(new LinkedList<>());
        }
        assert paths.stream().allMatch(x -> x.stream().noneMatch(Path::isEmpty));

    }

    private void addPath(Path found) {
        assert !found.isEmpty();
        assert directed || found.head().data() > found.tail().data();
        int lastPlacedIndex = vertexMatching.getPlacementUnsafe().size() - 1;
        Path added = new Path(found);
        paths.get(lastPlacedIndex).add(added);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EdgeMatching " + (directed ? "directed " : "") + "{\n");
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
        for (Iterator<PathIterator> i = headMap[vertexData].iterator(); i.hasNext();) {
            PathIterator pathIt = i.next();
            pathfinders.get(pathIt.tail(), pathIt.head()).reset();
            i.remove();
        }
        for (Iterator<PathIterator> i = tailMap[vertexData].iterator(); i.hasNext();) {
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

    public Object getState() {
        List<Object> total = new LinkedList<>();
        total.add(paths);
        total.add(pathfinders.values().stream().map(Stateable::getState).collect(Collectors.toSet()));
        return total;
    }

    public void removeLastPath() {
        List<Path> pathList = this.paths.get(this.vertexMatching.getPlacementUnsafe().size() - 1);
        Path removed = pathList.remove(pathList.size() - 1);
        //removed.intermediate().forEach(x -> occupation.releaseRouting(vertexMatching.getPlacementUnsafe().size(), x)); // this should be the empty path now
        assert directed || removed.head().data() > removed.tail().data();
    }


}
