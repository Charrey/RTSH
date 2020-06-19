package com.charrey.pathiterators.controlpoint;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.occupation.AbstractOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.PathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.*;
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
    private final OccupationTransaction occupation;
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

    public static boolean log = false;

    ControlPointIterator(@NotNull MyGraph targetGraph,
                         @NotNull Vertex tail,
                         @NotNull Vertex head,
                         @NotNull OccupationTransaction occupation,
                         @NotNull Set<Integer> initialLocalOccupation,
                         int controlPoints,
                         Supplier<Integer> verticesPlaced,
                         boolean refuseLongerPaths) {
        super(tail, head, refuseLongerPaths);
        this.targetGraph = targetGraph;
        this.controlPoints = controlPoints;
        this.localOccupation = initialLocalOccupation;
        this.occupation = occupation;
        this.head = head;
        this.controlPointCandidates = new ControlPointVertexSelector(targetGraph, occupation, Collections.unmodifiableSet(initialLocalOccupation), tail, head, refuseLongerPaths, tail);
        this.verticesPlaced = verticesPlaced;
    }

    private void setRightNeighbourOfRightNeighbour(Vertex vertex, Path localOccupiedForThatPath, Set<Integer> previousLocalOccupation) {
        this.rightNeighbourOfRightNeighbour = vertex;
        this.pathFromRightNeighbourToItsRightNeighbour = localOccupiedForThatPath;
        this.previousLocalOccupation = previousLocalOccupation;
    }

    @Nullable
    static Path filteredShortestPath(@NotNull MyGraph targetGraph, @NotNull AbstractOccupation globalOccupation, @NotNull Set<Integer> localOccupation, Vertex from, Vertex to, boolean refuseLongerPaths, Vertex tail) {
        assert targetGraph.containsVertex(from);
        assert targetGraph.containsVertex(to);
        Graph<Vertex, DefaultEdge> fakeGraph = new MaskSubgraph<>(targetGraph, x ->
                x != from &&
                        x != to &&
                        (localOccupation.contains(x.data()) || globalOccupation.isOccupied(x) ||
                                (refuseLongerPaths && violatesLongerPaths(targetGraph, x, from, to, tail, localOccupation))), x -> false);

        StringWriter writer = new StringWriter();
        new DOTExporter<Vertex, DefaultEdge>(vertex -> String.valueOf(vertex.data())).exportGraph(fakeGraph, writer);
        String dot = writer.toString();

        GraphPath<Vertex, DefaultEdge> algo = new BFSShortestPath<>(fakeGraph).getPath(from, to);
        return algo == null ? null : new Path(targetGraph, algo);
    }

    private static boolean violatesLongerPaths(Graph<Vertex, DefaultEdge> targetGraph, Vertex x, Vertex from, Vertex to, Vertex tail, Set<Integer> localOccupation) {
        if (tail != from && (targetGraph.getEdge(tail, x) != null)) {
            return true;
        }
        return localOccupation.stream().filter(y -> to.data() != y).anyMatch(y -> Graphs.successorListOf(targetGraph, x).stream().anyMatch(z -> z.data() == y));
    }

    @Nullable
    private Path filteredShortestPath(Vertex from, Vertex to) {
        return filteredShortestPath(targetGraph, occupation, localOccupation, from, to, refuseLongerPaths, tail());
    }

    @Nullable
    @Override
    public Path next() {
        StringBuilder prefix = new StringBuilder();
        prefix.append("   ".repeat(Math.max(0, 10 - controlPoints)));
        while (!done) {
            if (controlPoints == 0) {
                if (log) {
                    System.out.print(prefix.toString() + "querying shortest path...");
                }
                done = true;
                Path shortestPath = filteredShortestPath(tail(), head);
                assert shortestPath == null || new Path(shortestPath).intermediate().stream().noneMatch(x -> localOccupation.contains(x.data()));
                this.chosenPath = shortestPath == null ? null : new Path(shortestPath);
                if (refuseLongerPaths && this.chosenPath != null && isUnNecessarilyLong(this.chosenPath)) {
                    chosenPath = null;
                }
                if (chosenPath != null) {
                    try {
                        occupation.occupyRoutingAndCheck(verticesPlaced.get(), chosenPath);
                    } catch (DomainCheckerException e) {
                        chosenPath = null;
                    }
                }
                if (chosenPath == null) {
                    if (log) {
                        System.out.println("...failed");
                    }
                } else {
                    if (log) {
                        System.out.println("...succeeded!");
                    }
                }
                return chosenPath;
            } else if (child == null) {
                if (chosenControlPoint != null) {
                    if (!this.occupation.isOccupied(chosenControlPoint)) {
                        System.out.println("foo");
                    }
                    this.occupation.releaseRouting(verticesPlaced.get(), chosenControlPoint);
                }
                boolean makesNeighbourUseless;
                boolean rightShiftPossible;
                do {
                    chosenControlPoint = controlPointCandidates.next();
                    if (log) {
                        System.out.println(prefix.toString() + "Chosen control point: " + chosenControlPoint);
                    }
                    makesNeighbourUseless   = makesNeighbourUseless(chosenControlPoint);
                    rightShiftPossible      = rightShiftPossible(chosenControlPoint);
                    if (makesNeighbourUseless && log) {
                        System.out.println(prefix.toString() + "It makes right neighbour " + head() + " useless: it is already on the shortest path between " + chosenControlPoint + " and " + rightNeighbourOfRightNeighbour);
                    }
                    if (!makesNeighbourUseless && rightShiftPossible && log) {
                        System.out.println(prefix.toString() + "Right shift possible.");
                    }
                } while (chosenControlPoint != null && (makesNeighbourUseless || rightShiftPossible));
                if (chosenControlPoint == null) {
                    done = true;
                    return null;
                }
                try {
                    this.occupation.occupyRoutingAndCheck(verticesPlaced.get(), chosenControlPoint);
                } catch (DomainCheckerException e) {
                    if (log) {
                        System.out.println(prefix.toString() + "Domain check failed---------------------------------------------------------------------------");
                    }
                    chosenControlPoint = null;
                    continue;
                }
                Path graphPath = filteredShortestPath(chosenControlPoint, head);
                if (graphPath != null) {
                    chosenPath = new Path(graphPath);
                    if (refuseLongerPaths && isUnNecessarilyLong(chosenPath)) {
                        continue;
                    }
                    try {
                        occupation.occupyRoutingAndCheck(verticesPlaced.get(), chosenPath);
                    } catch (DomainCheckerException e) {
                        continue;
                    }

                    Set<Integer> previousOccupation = new HashSet<>(localOccupation);
                    chosenPath.forEach(x -> localOccupation.add(x.data()));
                    child = new ControlPointIterator(targetGraph, tail(), chosenControlPoint, occupation, new HashSet<>(localOccupation), controlPoints - 1, verticesPlaced, refuseLongerPaths);
                    child.setRightNeighbourOfRightNeighbour(this.head(), chosenPath, previousOccupation);
                } else {
                    if (log) {
                        System.out.println(prefix.toString() + "Could not find route from control point.");
                    }
                }
            } else {
                Set<Integer> previousLocalOccupation = new HashSet<>(localOccupation);
                Path childsPath = child.next();
                assert childsPath == null || childsPath.intermediate().stream().noneMatch(x -> previousLocalOccupation.contains(x.data()));
                assert chosenPath != null;
                if (childsPath != null) {
                    assert childsPath.first() == tail();
                    assert chosenPath.last() == head;
                    assert childsPath.last() == chosenPath.first();
                    Path toReturn = merge(targetGraph, childsPath, chosenPath);
                    assert toReturn.first() == tail();
                    assert toReturn.last() == head;
                    return toReturn;
                } else {
                    child = null;
                    chosenPath.forEach(x -> localOccupation.remove(x.data()));
                    chosenPath.intermediate().forEach(x -> occupation.releaseRouting(verticesPlaced.get(), x));
                }
            }
        }
        if (controlPoints == 0 && chosenPath != null) {
            chosenPath.intermediate().forEach(x -> occupation.releaseRouting(verticesPlaced.get(), x));
            chosenPath = null;
        }
        return null;
    }

    private boolean isUnNecessarilyLong(@NotNull Path chosenPath) {
        List<Vertex> fromCandidates = chosenPath.asList().subList(0, chosenPath.length() - 1);
        for (Vertex candidate : fromCandidates) {
            List<Vertex> successors = Graphs.successorListOf(targetGraph, candidate);
            for (Vertex successor : successors) {
                boolean isHead = successor == chosenPath.last();
                boolean localOccupationContains = localOccupation.contains(successor.data());
                if (!isHead && localOccupationContains) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean rightShiftPossible(@Nullable Vertex left) {
        if (left != null && this.rightNeighbourOfRightNeighbour != null) {
            Vertex middle = this.head();
            assert pathFromRightNeighbourToItsRightNeighbour != null;
            assert previousLocalOccupation != null;
            Path middleToRight = pathFromRightNeighbourToItsRightNeighbour;
            for (int i = 0; i < middleToRight.intermediate().size(); i++) {
                Vertex middleAlt = middleToRight.intermediate().get(i);
                Path middleAltToRight = new Path(targetGraph, middleToRight.asList().subList(i + 1, middleToRight.length()));

                Set<Integer> fictionalOccupation = new HashSet<>(previousLocalOccupation);
                middleAltToRight.forEach(x -> fictionalOccupation.add(x.data()));
                Collection<Vertex >temporarilyRemoveGlobal = middleToRight.asList().subList(0, middleToRight.length() - 1);
                temporarilyRemoveGlobal.forEach(x -> occupation.releaseRouting(verticesPlaced.get(), x));
                Path leftToMiddleAlt = filteredShortestPath(targetGraph, occupation, fictionalOccupation, left, middleAlt, refuseLongerPaths, tail());
                if (leftToMiddleAlt == null) {
                    temporarilyRemoveGlobal.forEach(x -> {
                        try {
                            occupation.occupyRoutingAndCheck(verticesPlaced.get(), x);
                        } catch (DomainCheckerException e) {
                            assert false;
                        }
                    });
                    return true;
                }
                Path leftToRightAlt = merge(targetGraph, leftToMiddleAlt, middleAltToRight);
                Path leftToMiddle = filteredShortestPath(left, middle);
                temporarilyRemoveGlobal.forEach(x -> {
                    try {
                        occupation.occupyRoutingAndCheck(verticesPlaced.get(), x);
                    } catch (DomainCheckerException e) {
                        assert false;
                    }
                });
                if (leftToMiddle == null) {
                    return true;
                }
                Path leftToRight = merge(targetGraph, leftToMiddle, middleToRight);
                if (leftToRight.equals(leftToRightAlt)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean makesNeighbourUseless(@Nullable Vertex left) {
        if (this.rightNeighbourOfRightNeighbour == null || left == null) {
            return false;
        } else {
            assert pathFromRightNeighbourToItsRightNeighbour != null;
            Vertex middle = this.head();
            Vertex right = this.rightNeighbourOfRightNeighbour;
            this.pathFromRightNeighbourToItsRightNeighbour.forEach(x -> localOccupation.remove(x.data()));

            Collection<Vertex> temporaryRemoveFromGlobal = pathFromRightNeighbourToItsRightNeighbour.asList().subList(0, pathFromRightNeighbourToItsRightNeighbour.length()-1);
            temporaryRemoveFromGlobal.forEach(x -> occupation.releaseRouting(verticesPlaced.get(), x));
            Path skippedPath = filteredShortestPath(targetGraph, occupation, localOccupation, left, right, refuseLongerPaths, tail());
            this.pathFromRightNeighbourToItsRightNeighbour.forEach(x -> localOccupation.add(x.data()));
            temporaryRemoveFromGlobal.forEach(x -> {
                try {
                    occupation.occupyRoutingAndCheck(verticesPlaced.get(), x);
                } catch (DomainCheckerException e) {
                    e.printStackTrace();
                    assert false;
                }
            });
            if (skippedPath == null) {
                return true;
            } else {
                return skippedPath.contains(middle);
            }
        }
    }

    @NotNull
    static Path merge(@NotNull MyGraph graph, @NotNull Path left, @NotNull Path right) {
        Path toReturn = new Path(graph, left.first());
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

    Path finalPath() {
        return child == null ? this.chosenPath : child.finalPath();
    }
}
