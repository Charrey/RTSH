package com.charrey.pathiterators.controlpoint;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
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

import java.util.*;
import java.util.function.Supplier;

public class ControlPointIterator extends PathIterator {

    private final int controlPoints;
    @NotNull
    private final Set<Integer> localOccupation;
    private final int head;
    @NotNull
    private final MyGraph targetGraph;
    @NotNull
    private final OccupationTransaction occupation;
    private final Supplier<Integer> verticesPlaced;

    private boolean done = false;
    @NotNull
    private final Iterator<Integer> controlPointCandidates;

    @Nullable
    private ControlPointIterator child = null;
    private int chosenControlPoint = -1;
    @Nullable
    private Path chosenPath = null;

    @Nullable
    Path getChosenPath() {
        return chosenPath;
    }

    //for filtering
    private int rightNeighbourOfRightNeighbour = -1;
    @Nullable
    private Path pathFromRightNeighbourToItsRightNeighbour = null;
    @Nullable
    private Set<Integer> previousLocalOccupation = null;

    public static boolean log = false;

    ControlPointIterator(@NotNull MyGraph targetGraph,
                         int tail,
                         int head,
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

    private void setRightNeighbourOfRightNeighbour(int vertex, Path localOccupiedForThatPath, Set<Integer> previousLocalOccupation) {
        this.rightNeighbourOfRightNeighbour = vertex;
        this.pathFromRightNeighbourToItsRightNeighbour = localOccupiedForThatPath;
        this.previousLocalOccupation = previousLocalOccupation;
    }

    @Nullable
    static Path filteredShortestPath(@NotNull MyGraph targetGraph, @NotNull AbstractOccupation globalOccupation, @NotNull Set<Integer> localOccupation, int from, int to, boolean refuseLongerPaths, int tail) {
        assert targetGraph.containsVertex(from);
        assert targetGraph.containsVertex(to);
        Graph<Integer, DefaultEdge> fakeGraph = new MaskSubgraph<>(targetGraph, x ->
                x != from &&
                        x != to &&
                        (localOccupation.contains(x) || globalOccupation.isOccupied(x) ||
                                (refuseLongerPaths && violatesLongerPaths(targetGraph, x, from, to, tail, localOccupation))), x -> false);

//        StringWriter writer = new StringWriter();
//        new DOTExporter<Vertex, DefaultEdge>(vertex -> String.valueOf(vertex.data())).exportGraph(fakeGraph, writer);
//        String dot = writer.toString();

        GraphPath<Integer, DefaultEdge> algo = new BFSShortestPath<>(fakeGraph).getPath(from, to);
        return algo == null ? null : new Path(targetGraph, algo);
    }

    private static boolean violatesLongerPaths(Graph<Integer, DefaultEdge> targetGraph, int x, int from, int to, int tail, Set<Integer> localOccupation) {
        if (tail != from && (targetGraph.getEdge(tail, x) != null)) {
            return true;
        }
        return localOccupation.stream().filter(y -> to != y).anyMatch(y -> Graphs.successorListOf(targetGraph, x).stream().anyMatch(z -> z.equals(y)));
    }

    @Nullable
    private Path filteredShortestPath(int from, int to) {
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
                assert shortestPath == null || new Path(shortestPath).intermediate().stream().noneMatch(localOccupation::contains);
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
                if (chosenControlPoint != -1) {
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
                } while (chosenControlPoint != -1 && (makesNeighbourUseless || rightShiftPossible));
                if (chosenControlPoint == -1) {
                    done = true;
                    return null;
                }
                try {
                    this.occupation.occupyRoutingAndCheck(verticesPlaced.get(), chosenControlPoint);
                } catch (DomainCheckerException e) {
                    if (log) {
                        System.out.println(prefix.toString() + "Domain check failed---------------------------------------------------------------------------");
                    }
                    chosenControlPoint = -1;
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
                    chosenPath.forEach(localOccupation::add);
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
                assert childsPath == null || childsPath.intermediate().stream().noneMatch(previousLocalOccupation::contains);
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
                    chosenPath.forEach(localOccupation::remove);
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

    @Override
    public String debugInfo() {
        throw new IllegalStateException("Call ManagedControlPointIterator insead");
    }

    private boolean isUnNecessarilyLong(@NotNull Path chosenPath) {
        List<Integer> fromCandidates = chosenPath.asList().subList(0, chosenPath.length() - 1);
        for (int candidate : fromCandidates) {
            List<Integer> successors = Graphs.successorListOf(targetGraph, candidate);
            for (int successor : successors) {
                boolean isHead = successor == chosenPath.last();
                boolean localOccupationContains = localOccupation.contains(successor);
                if (!isHead && localOccupationContains) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean rightShiftPossible(int left) {
        if (left != -1 && this.rightNeighbourOfRightNeighbour != -1) {
            int middle = this.head();
            assert pathFromRightNeighbourToItsRightNeighbour != null;
            assert previousLocalOccupation != null;
            Path middleToRight = pathFromRightNeighbourToItsRightNeighbour;
            for (int i = 0; i < middleToRight.intermediate().size(); i++) {
                int middleAlt = middleToRight.intermediate().get(i);
                Path middleAltToRight = new Path(targetGraph, middleToRight.asList().subList(i + 1, middleToRight.length()));

                Set<Integer> fictionalOccupation = new HashSet<>(previousLocalOccupation);
                middleAltToRight.forEach(fictionalOccupation::add);
                Collection<Integer> temporarilyRemoveGlobal = middleToRight.asList().subList(0, middleToRight.length() - 1);
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

    private boolean makesNeighbourUseless(int left) {
        if (this.rightNeighbourOfRightNeighbour == -1 || left == -1) {
            return false;
        } else {
            assert pathFromRightNeighbourToItsRightNeighbour != null;
            int middle = this.head();
            int right = this.rightNeighbourOfRightNeighbour;
            this.pathFromRightNeighbourToItsRightNeighbour.forEach(localOccupation::remove);

            Collection<Integer> temporaryRemoveFromGlobal = pathFromRightNeighbourToItsRightNeighbour.asList().subList(0, pathFromRightNeighbourToItsRightNeighbour.length()-1);
            temporaryRemoveFromGlobal.forEach(x -> occupation.releaseRouting(verticesPlaced.get(), x));
            Path skippedPath = filteredShortestPath(targetGraph, occupation, localOccupation, left, right, refuseLongerPaths, tail());
            this.pathFromRightNeighbourToItsRightNeighbour.forEach(localOccupation::add);
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
    List<Integer> controlPoints() {
        List<Integer> res = new LinkedList<>();
        if (child != null) {
            res.addAll(child.controlPoints());
        }
        if (chosenControlPoint != -1) {
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
