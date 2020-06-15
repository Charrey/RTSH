package com.charrey.pathiterators.controlpoint;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.PathIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.MaskSubgraph;

import java.util.*;
import java.util.function.Supplier;

public class ControlPointIterator extends PathIterator {

    private final int controlPoints;
    @NotNull
    private final Set<Integer> localOccupation;
    @NotNull
    private final Vertex head;
    @NotNull
    private final MyGraph targetGraph;
    @NotNull
    private final Occupation globalOccupation;
    private final Supplier<Integer> verticesPlaced;

    private boolean done = false;
    @NotNull
    private final Iterator<Vertex> controlPointCandidates;

    @Nullable
    private ControlPointIterator child = null;
    @Nullable
    private Vertex chosenControlPoint = null;
    @Nullable
    private Path chosenPath = null;

    @Nullable
    Path getChosenPath() {
        return chosenPath;
    }

    //for filtering
    @Nullable
    private Vertex rightNeighbourOfRightNeighbour = null;
    @Nullable
    private Path pathFromRightNeighbourToItsRightNeighbour = null;
    @Nullable
    private Set<Integer> previousLocalOccupation = null;


    ControlPointIterator(@NotNull MyGraph targetGraph,
                         @NotNull Vertex tail,
                         @NotNull Vertex head,
                         @NotNull Occupation globalOccupation,
                         @NotNull Set<Integer> initialLocalOccupation,
                         int controlPoints,
                         Supplier<Integer> verticesPlaced,
                         boolean refuseLongerPaths) {
        super(tail, head, refuseLongerPaths);
        this.targetGraph = targetGraph;
        this.controlPoints = controlPoints;
        this.localOccupation = initialLocalOccupation;
        this.globalOccupation = globalOccupation;
        this.head = head;
        this.controlPointCandidates = new ControlPointVertexSelector(targetGraph, globalOccupation, Collections.unmodifiableSet(initialLocalOccupation), tail, head);
        this.verticesPlaced = verticesPlaced;
    }

    private void setRightNeighbourOfRightNeighbour(Vertex vertex, Path localOccupiedForThatPath, Set<Integer> previousLocalOccupation) {
        this.rightNeighbourOfRightNeighbour = vertex;
        this.pathFromRightNeighbourToItsRightNeighbour = localOccupiedForThatPath;
        this.previousLocalOccupation = previousLocalOccupation;
    }

    @Nullable
    static Path filteredShortestPath(@NotNull Graph<Vertex, DefaultEdge> targetGraph, @NotNull Occupation globalOccupation, @NotNull Set<Integer> localOccupation, Vertex from, Vertex to) {
        assert targetGraph.containsVertex(from);
        assert targetGraph.containsVertex(to);
        Graph<Vertex, DefaultEdge> fakeGraph = new MaskSubgraph<>(targetGraph, x -> x != from && x != to && (localOccupation.contains(x.data()) || globalOccupation.isOccupied(x)), x -> false);
        GraphPath<Vertex, DefaultEdge> algo = new BFSShortestPath<>(fakeGraph).getPath(from, to);
        return algo == null ? null : new Path(algo);
    }

    @Nullable
    private Path filteredShortestPath(Vertex from, Vertex to) {
        return filteredShortestPath(targetGraph, globalOccupation, localOccupation, from, to);
    }

    @Nullable
    @Override
    public Path next() {
        while (!done) {
            if (controlPoints == 0) {
                done = true;
                Path shortestPath = filteredShortestPath(tail(), head);
                assert shortestPath == null || new Path(shortestPath).intermediate().stream().noneMatch(x -> localOccupation.contains(x.data()));
                this.chosenPath = shortestPath == null ? null : new Path(shortestPath);
                if (refuseLongerPaths && this.chosenPath != null && isUnNecessarilyLong(this.chosenPath)) {
                    chosenPath = null;
                }
                return chosenPath;
            } else if (child == null) {
                do {
                    chosenControlPoint = controlPointCandidates.next();
                } while (chosenControlPoint != null && (makesNeighbourUseless(chosenControlPoint)) || rightShiftPossible(chosenControlPoint));
                if (chosenControlPoint == null) {
                    done = true;
                    return null;
                }
                Path graphPath = filteredShortestPath(chosenControlPoint, head);
                if (graphPath != null) {
                    chosenPath = new Path(graphPath);
                    if (refuseLongerPaths && isUnNecessarilyLong(chosenPath)) {
                        continue;
                    }
                    Set<Integer> previousOccupation = new HashSet<>(localOccupation);
                    chosenPath.forEach(x -> localOccupation.add(x.data()));
                    child = new ControlPointIterator(targetGraph, tail(), chosenControlPoint, globalOccupation, new HashSet<>(localOccupation), controlPoints - 1, verticesPlaced, refuseLongerPaths);
                    child.setRightNeighbourOfRightNeighbour(this.head(), chosenPath, previousOccupation);
                }
            } else {
                Set<Integer> previousLocalOccupation = new HashSet<>(localOccupation);
                Path childsPath = child.next();
                assert childsPath == null || childsPath.intermediate().stream().noneMatch(x -> previousLocalOccupation.contains(x.data()));
                assert chosenPath != null;
                if (childsPath != null) {
                    assert childsPath.tail() == tail();
                    assert chosenPath.head() == head;
                    assert childsPath.head() == chosenPath.tail();
                    Path toReturn =  merge(childsPath, chosenPath);
                    assert toReturn.tail() == tail();
                    assert toReturn.head() == head;
                    return toReturn;
                } else {
                    child = null;
                    chosenPath.forEach(x -> localOccupation.remove(x.data()));
                }
            }
        }
        return null;
    }

    private boolean isUnNecessarilyLong(@NotNull Path chosenPath) {
        return chosenPath.asList().subList(0, chosenPath.length() - 1)
                .stream()
                .anyMatch(x -> targetGraph.outgoingEdgesOf(x).stream().map(y -> Graphs.getOppositeVertex(targetGraph, y, x)).anyMatch(y -> y != chosenPath.head() && localOccupation.contains(y.data())));
    }

    private boolean rightShiftPossible(@Nullable Vertex left) {
        if (left != null && this.rightNeighbourOfRightNeighbour != null) {
            Vertex middle = this.head();
            assert pathFromRightNeighbourToItsRightNeighbour != null;
            assert previousLocalOccupation != null;
            Path middleToRight = pathFromRightNeighbourToItsRightNeighbour;
            for (int i = 0; i < middleToRight.intermediate().size(); i++) {
                Vertex middleAlt = middleToRight.intermediate().get(i);
                Path middleAltToRight = new Path(middleToRight.asList().subList(i + 1, middleToRight.length()));
                Set<Integer> fictionalOccupation = new HashSet<>(previousLocalOccupation);
                middleAltToRight.forEach(x -> fictionalOccupation.add(x.data()));
                Path leftToMiddleAlt = filteredShortestPath(targetGraph, globalOccupation, fictionalOccupation, left, middleAlt);
                if (leftToMiddleAlt == null) {
                    return true;
                }
                Path leftToRightAlt = merge(leftToMiddleAlt, middleAltToRight);
                Path leftToMiddle = filteredShortestPath(left, middle);
                if (leftToMiddle == null) {
                    return true;
                }
                Path leftToRight = merge(leftToMiddle, middleToRight);
                if (leftToRight.equals(leftToRightAlt)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean makesNeighbourUseless(Vertex left) {
        if (this.rightNeighbourOfRightNeighbour == null) {
            return false;
        } else {
            assert pathFromRightNeighbourToItsRightNeighbour != null;
            Vertex middle = this.head();
            Vertex right = this.rightNeighbourOfRightNeighbour;
            this.pathFromRightNeighbourToItsRightNeighbour.forEach(x -> localOccupation.remove(x.data()));
            Path skippedPath = filteredShortestPath(targetGraph, globalOccupation, localOccupation, left, right);
            this.pathFromRightNeighbourToItsRightNeighbour.forEach(x -> localOccupation.add(x.data()));
            if (skippedPath == null) {
                return true;
            } else {
                return skippedPath.contains(middle);
            }
        }
    }

    @NotNull
    static Path merge(@NotNull Path left, @NotNull Path right) {
        Path toReturn = new Path(left.tail(), left.length() + right.length() - 1);
        for (int i = 1; i < left.length() - 1; i++) {
            toReturn.append(left.get(i));
        }
        for (int i = 0; i < right.length(); i++) {
            toReturn.append(right.get(i));
        }
        return toReturn;
    }

    @NotNull
    @Override
    public String toString() {
        return "(" + chosenControlPoint + ", " + child + ")";
    }

    @NotNull
    List<Vertex> controlPoints() {
        List<Vertex> res = new LinkedList<>();
        if (child != null) {
            res.addAll(child.controlPoints());
        }
        if (chosenControlPoint != null) {
            res.add(chosenControlPoint);
        }
        return res;
    }

    @Nullable
    public ControlPointIterator getChild() {
        return child;
    }

    @NotNull
    Set<Integer> getLocalOccupation() {
        return Collections.unmodifiableSet(localOccupation);
    }
}
