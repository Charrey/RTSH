package com.charrey.matching;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.util.datastructures.MultipleKeyMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class EdgeMatching extends VertexBlocker {

    private final VertexMatching vertexMatching;
    private final MyGraph source;
    @NotNull
    private final MyGraph targetGraph;
    private final boolean directed;
    private final boolean refuseLongerPaths;
    private final int pathIteration;

    private MultipleKeyMap<PathIterator> pathfinders;
    private final GlobalOccupation occupation;

    private Vertex[][] edges; //do not change
    private boolean[][] incoming; //only for directed graphs

    private ArrayList<LinkedList<Pair<Path, String>>> paths;

    private final UtilityData data;

    public EdgeMatching(VertexMatching vertexMatching, UtilityData data, MyGraph source, @NotNull MyGraph target, GlobalOccupation occupation, int pathIteration, boolean refuseLongerPaths) {
        this.vertexMatching = vertexMatching;
        this.source = source;
        this.data = data;
        this.targetGraph = target;
        this.directed = target.isDirected();
        initPathsEdges();
        initPathFinders();
        assert paths.stream().allMatch(x -> x.stream().noneMatch(y -> y.getFirst().isEmpty()));
        EdgeMatching em = this;
        this.vertexMatching.setOnDeletion(em::synchronize);
        this.occupation = occupation;
        this.pathIteration = pathIteration;
        this.refuseLongerPaths = refuseLongerPaths;
    }

    private void initPathFinders() {
        int targetSize = targetGraph.vertexSet().size();
        pathfinders = new MultipleKeyMap<>(targetSize, targetSize, PathIterator.class);
        while (paths.size() < data.getOrder().size()) {
            paths.add(new LinkedList<>());
        }
        assert paths.stream().allMatch(x -> x.stream().noneMatch(y -> y.getFirst().isEmpty()));
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
        List<Pair<Path, String>> pathList = paths.get(vertexMatching.getPlacementUnsafe().size()-1);
        if (pathList.isEmpty()) {
            return false;
        }
        int placementSize = vertexMatching.getPlacementUnsafe().size();
        assert occupation.domainChecker.checkOK(placementSize);
        for (int i = pathList.size() - 1; i >= 0; i--) {
            Path toRetry = pathList.get(i).getFirst();
            Vertex tail = toRetry.first();
            Vertex head = toRetry.last();
            assert directed || tail.data() < head.data();
            assert pathfinders.containsKey(tail, head);
            PathIterator pathfinder = pathfinders.get(tail, head);
            Path pathFound = pathfinder.next();
            if (pathFound != null) {
                assert pathFound.first() == tail : "Expected: " + tail + ", actual: " + pathFound.first();
                assert pathFound.last() == head : "Expected: " + head + ", actual: " + pathFound.last();
                Path toAdd = new Path(pathFound);
                pathList.set(pathList.size() - 1, new Pair<>(toAdd, pathfinder.debugInfo()));
                assert occupation.domainChecker.checkOK(placementSize);
                return true;
            } else {
                pathfinders.remove(tail, head);
                removeLastPath();
            }
        }
        assert occupation.domainChecker.checkOK(placementSize);
        return false;
    }

    @Nullable
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
        assert directed || tail.data() < head.data();
        //get pathIterator

        if (pathfinders.containsKey(tail, head)) {
            pathfinders.remove(tail, head);
            assert false;
        }
        PathIterator iterator = PathIterator.get(targetGraph, data, tail, head, occupation, () -> vertexMatching.getPlacementUnsafe().size(), pathIteration, refuseLongerPaths);
        pathfinders.put(tail, head, iterator);
        Path toReturn = iterator.next();
        if (toReturn != null) {
            addPath(toReturn, iterator.debugInfo());
            assert occupation.domainChecker.checkOK(vertexMatching.getPlacementUnsafe().size());
            return toReturn;
        } else {
            pathfinders.remove(tail, head);
            //iterator.reset();
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
        assert paths.stream().allMatch(x -> x.stream().noneMatch(y -> y.getFirst().isEmpty()));

    }

    private void addPath(@NotNull Path found, String debugInfo) {
        assert !found.isEmpty();
        assert directed || found.last().data() > found.first().data();
        int lastPlacedIndex = vertexMatching.getPlacementUnsafe().size() - 1;
        Path added = new Path(found);
        paths.get(lastPlacedIndex).add(new Pair<>(added, debugInfo));
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EdgeMatching " + (directed ? "directed " : "") + "{\n");
        for (List<Pair<Path, String>> pathAddition : paths) {
            sb.append("\t").append(pathAddition).append("\n");
        }
        sb.append("}\n");
        assert paths.stream().allMatch(x -> x.stream().noneMatch(y -> y.getFirst().isEmpty()));
        return sb.toString();
    }

    private void synchronize(Vertex vertex) {
        assert occupation.domainChecker.checkOK(vertexMatching.getPlacementUnsafe().size());
        assert !vertexMatching.getPlacementUnsafe().contains(vertex);
        paths.get(vertexMatching.getPlacementUnsafe().size()).clear();
        assert occupation.domainChecker.checkOK(vertexMatching.getPlacementUnsafe().size());
    }

    @NotNull
    public Set<Path> allPaths() {
        Set<Path> res = new HashSet<>();
        for (LinkedList<Pair<Path, String>> pathList : paths) {
            pathList.forEach(x -> res.add(x.getFirst()));
        }
        return Collections.unmodifiableSet(res);
    }

    private void removeLastPath() {
        List<Pair<Path, String>> pathList = this.paths.get(this.vertexMatching.getPlacementUnsafe().size() - 1);
        Path removed = pathList.remove(pathList.size() - 1).getFirst();
        //removed.intermediate().forEach(x -> occupation.releaseRouting(vertexMatching.getPlacementUnsafe().size(), x)); // this should be the empty path now
        assert directed || removed.last().data() > removed.first().data();
    }


}
