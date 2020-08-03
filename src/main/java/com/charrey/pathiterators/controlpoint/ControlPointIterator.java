package com.charrey.pathiterators.controlpoint;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.settings.Settings;
import com.charrey.util.Util;
import gnu.trove.TCollections;
import gnu.trove.list.TIntList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graphs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * A pathiterator that iterates paths between two vertices with a fixed number of intermediate vertices it has to visit.
 * Furthermore, it avoids paths that could be obtained with a lower number of intermediate vertices.
 */
class ControlPointIterator extends PathIterator {

    public static final int ABSENT = -1;
    public static final int EXHAUSTED = -2;
    private final int controlPoints;
    @NotNull
    private final TIntSet localOccupation;
    private final int head;
    @NotNull
    private final MyGraph targetGraph;

    private final Supplier<Integer> verticesPlaced;
    private static final Logger LOG = Logger.getLogger("ControlPointIterator");
    private final GlobalOccupation globalOccupation;

    private boolean done = false;
    @NotNull
    private final ControlPointVertexSelector controlPointCandidates;

    @Nullable
    private ControlPointIterator child = null;
    private int chosenControlPoint = ABSENT;
    @Nullable
    private Path chosenPath = null;
    //for filtering
    private int rightNeighbourOfRightNeighbour = ABSENT;
    @Nullable
    private Path pathFromRightNeighbourToItsRightNeighbour = null;
    @Nullable
    private TIntSet previousLocalOccupation = null;
    private final Settings settings;

    /**
     * Instantiates a new ControlPointIterator.
     *
     * @param targetGraph            the target graph where homeomorphisms are found
     * @param tail                   the source vertex
     * @param head                   the target vertex
     * @param occupation             the occupationTransaction where intermediate nodes are registered
     * @param initialLocalOccupation a set used to keep track of vertices used in this very path
     * @param controlPoints          the number of control points that should be used
     * @param verticesPlaced         a supplier of the number of source graph vertices currently matched
     * @param settings               the settings of the homeomorphism search
     */
    ControlPointIterator(@NotNull MyGraph targetGraph,
                         int tail,
                         int head,
                         @NotNull GlobalOccupation globalOccupation,
                         @NotNull OccupationTransaction occupation,
                         @NotNull TIntSet initialLocalOccupation,
                         int controlPoints,
                         Supplier<Integer> verticesPlaced,
                         Settings settings,
                         PartialMatchingProvider provider) {
        super(tail, head, settings, globalOccupation, occupation, provider);
        this.targetGraph = targetGraph;
        this.controlPoints = controlPoints;
        this.globalOccupation = globalOccupation;
        this.localOccupation = initialLocalOccupation;
        this.head = head;
        this.controlPointCandidates = new ControlPointVertexSelector(targetGraph, occupation, TCollections.unmodifiableSet(initialLocalOccupation), tail, head, settings.getRefuseLongerPaths(), tail);
        this.verticesPlaced = verticesPlaced;
        this.settings = settings;
    }

    private void setRightNeighbourOfRightNeighbour(int vertex, Path localOccupiedForThatPath, TIntSet previousLocalOccupation) {
        this.rightNeighbourOfRightNeighbour = vertex;
        this.pathFromRightNeighbourToItsRightNeighbour = localOccupiedForThatPath;
        this.previousLocalOccupation = previousLocalOccupation;
    }

    @NotNull
    private Optional<Path> filteredShortestPath(int from, int to) {
        return Util.filteredShortestPath(targetGraph, transaction, localOccupation, from, to, settings.getRefuseLongerPaths(), tail());
    }

    /**
     * Returns the path this iterator has currently picked between its control point and its target.
     *
     * @return the path between the picked control point and the target.
     */
    @Nullable
    Path getChosenPath() {
        return chosenPath;
    }

    @Override
    public String debugInfo() {
        throw new IllegalStateException("Call ManagedControlPointIterator insead");
    }

    private boolean isUnNecessarilyLong(@NotNull Path chosenPath) {
        Path fromCandidates = chosenPath.subPath(0, chosenPath.length() - 1);
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

    private boolean rightShiftPossible(int leftCandidate) {
        if (leftCandidate != ABSENT && this.rightNeighbourOfRightNeighbour != -1) {
            int middle = this.head();
            assert pathFromRightNeighbourToItsRightNeighbour != null;
            assert previousLocalOccupation != null;
            Path middleToRight = pathFromRightNeighbourToItsRightNeighbour;
            for (int i = 0; i < middleToRight.intermediate().length(); i++) {
                int middleAlt = middleToRight.intermediate().get(i);
                Path middleAltToRight = new Path(targetGraph, middleToRight.asList().subList(i + 1, middleToRight.length()));

                TIntSet fictionalOccupation = new TIntHashSet(previousLocalOccupation);
                middleAltToRight.forEach(fictionalOccupation::add);
                Path temporarilyRemoveGlobal = middleToRight.subPath(0, middleToRight.length() - 1);
                temporarilyRemoveGlobal.forEach(x -> transaction.releaseRouting(verticesPlaced.get(), x));
                Optional<Path> leftToMiddleAlt = Util.filteredShortestPath(targetGraph, transaction, fictionalOccupation, leftCandidate, middleAlt, settings.getRefuseLongerPaths(), tail());
                if (leftToMiddleAlt.isEmpty()) {
                    temporarilyRemoveGlobal.forEach(x -> {
                        try {
                            transaction.occupyRoutingAndCheck(verticesPlaced.get(), x, getPartialMatching());
                        } catch (DomainCheckerException e) {
                            assert false;
                        }
                    });
                    return true;
                }
                Path leftToRightAlt = Util.merge(targetGraph, leftToMiddleAlt.get(), middleAltToRight);
                Optional<Path> leftToMiddle = filteredShortestPath(leftCandidate, middle);
                temporarilyRemoveGlobal.forEach(x -> {
                    try {
                        transaction.occupyRoutingAndCheck(verticesPlaced.get(), x, getPartialMatching());
                    } catch (DomainCheckerException e) {
                        assert false;
                    }
                });
                if (leftToMiddle.isEmpty()) {
                    return true;
                }
                Path leftToRight = Util.merge(targetGraph, leftToMiddle.get(), middleToRight);
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

            Path temporaryRemoveFromGlobal = pathFromRightNeighbourToItsRightNeighbour.subPath(0, pathFromRightNeighbourToItsRightNeighbour.length() - 1);
            temporaryRemoveFromGlobal.forEach(x -> transaction.releaseRouting(verticesPlaced.get(), x));
            Optional<Path> skippedPath = Util.filteredShortestPath(targetGraph, transaction, localOccupation, left, right, settings.getRefuseLongerPaths(), tail());
            this.pathFromRightNeighbourToItsRightNeighbour.forEach(localOccupation::add);
            temporaryRemoveFromGlobal.forEach(x -> {
                try {
                    transaction.occupyRoutingAndCheck(verticesPlaced.get(), x, getPartialMatching());
                } catch (DomainCheckerException e) {
                    e.printStackTrace();
                    assert false;
                }
            });
            if (skippedPath.isEmpty()) {
                return true;
            } else {
                return skippedPath.get().contains(middle);
            }
        }
    }

    private boolean tryOccupy(int verticesPlaced, Path path) {
        try {
            transaction.occupyRoutingAndCheck(verticesPlaced, path, getPartialMatching());
            return true;
        } catch (DomainCheckerException e) {
            return false;
        }
    }



    private int findNextControlPoint(StringBuilder prefix) {
        do {
            chosenControlPoint = controlPointCandidates.next();
        } while (chosenControlPoint != ABSENT && (makesNeighbourUseless(chosenControlPoint) || rightShiftPossible(chosenControlPoint)));
        if (chosenControlPoint == ABSENT) {
            done = true;
            return EXHAUSTED;
        }
        try {
            this.transaction.occupyRoutingAndCheck(verticesPlaced.get(), chosenControlPoint, getPartialMatching());
        } catch (DomainCheckerException e) {
            LOG.finest(() -> prefix.toString() + "Domain check failed");
            chosenControlPoint = ControlPointIterator.ABSENT;
            return ControlPointIterator.ABSENT;
        }
        return 0;
    }

    @Nullable
    @Override
    public Path getNext() {
        StringBuilder prefix = new StringBuilder();
        prefix.append("   ".repeat(Math.max(0, 10 - controlPoints)));
        while (!done) {
            if (controlPoints == 0) {
                return provideShortestPath(prefix);
            } else if (child == null) {
                releasePreviousControlPoint();
                int result = findNextControlPoint(prefix);
                if (result == EXHAUSTED) {
                    return null;
                } else if (result == ABSENT) {
                    continue;
                }

                Optional<Path> graphPath = filteredShortestPath(chosenControlPoint, head);
                if (graphPath.isPresent()) {
                    chosenPath = graphPath.get();
                    if (settings.getRefuseLongerPaths() && isUnNecessarilyLong(chosenPath)) {
                        continue;
                    }
                    try {
                        transaction.occupyRoutingAndCheck(verticesPlaced.get(), chosenPath, getPartialMatching());
                    } catch (DomainCheckerException e) {
                        continue;
                    }

                    TIntSet previousOccupation = new TIntHashSet(localOccupation);
                    chosenPath.forEach(localOccupation::add);
                    child = new ControlPointIterator(targetGraph, tail(), chosenControlPoint, globalOccupation, transaction, new TIntHashSet(localOccupation), controlPoints - 1, verticesPlaced, settings, partialMatchingProvider);
                    child.setRightNeighbourOfRightNeighbour(this.head(), chosenPath, previousOccupation);
                }
            } else {
                Path toReturn = mergeWithChildsPath(prefix);
                if (toReturn != null) {
                    return toReturn;
                }
            }
        }
        if (controlPoints == 0 && chosenPath != null) {
            chosenPath.intermediate().forEach(x -> transaction.releaseRouting(verticesPlaced.get(), x));
            chosenPath = null;
        }
        return null;
    }

    private void releasePreviousControlPoint() {
        if (chosenControlPoint != ABSENT) {
            this.transaction.releaseRouting(verticesPlaced.get(), chosenControlPoint);
        }
    }

    @Nullable
    private Path mergeWithChildsPath(StringBuilder prefix) {
        TIntSet localOccupationBackup = new TIntHashSet(localOccupation);
        Path childsPath = child.next();
        assert childsPath == null || childsPath.intermediate().noneMatch(localOccupationBackup::contains);
        assert chosenPath != null;
        if (childsPath != null) {
            Path toReturn = Util.merge(targetGraph, childsPath, chosenPath);
            LOG.finest(() -> prefix.toString() + "Merged paths into " + toReturn);
            return toReturn;
        } else {
            child = null;
            chosenPath.forEach(localOccupation::remove);
            chosenPath.intermediate().forEach(x -> transaction.releaseRouting(verticesPlaced.get(), x));
            return null;
        }
    }

    @Nullable
    private Path provideShortestPath(StringBuilder prefix) {
        LOG.finest(() -> prefix.toString() + "querying shortest path...");
        done = true;
        Optional<Path> shortestPath = filteredShortestPath(tail(), head);
        assert shortestPath.isEmpty() || shortestPath.get().intermediate().noneMatch(localOccupation::contains);
        if (shortestPath.isEmpty() || (settings.getRefuseLongerPaths() && isUnNecessarilyLong(shortestPath.get())) || !tryOccupy(verticesPlaced.get(), shortestPath.get())) {
            LOG.finest("...failed");
            return null;
        } else {
            this.chosenPath = shortestPath.get();
            LOG.finest(() -> "...succeeded: " + chosenPath);
            return chosenPath;
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "(" + chosenControlPoint + ", " + child + ")";
    }

    /**
     * Lists all control points currently used from start of the path to end of the path.
     *
     * @return the list of control points
     */
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

    /**
     * Returns the recursive iterator that provides paths with one less control point.
     *
     * @return the recursive child
     */
    @Nullable
    public ControlPointIterator getChild() {
        return child;
    }

    /**
     * Returns the set of locally occupied vertices
     *
     * @return the local occupation
     */
    @NotNull
    TIntSet getLocalOccupation() {
        return TCollections.unmodifiableSet(localOccupation);
    }

    /**
     * Returns the path from start to finish of this control point iterator from its context. This includes the path
     * from controlpoint to the head vertex and the path yielded by the recursive child.
     *
     * @return the full path
     */
    Path finalPath() {
        return child == null ? this.chosenPath : child.finalPath();
    }
}
