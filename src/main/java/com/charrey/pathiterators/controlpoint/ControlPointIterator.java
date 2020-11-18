package com.charrey.pathiterators.controlpoint;

import com.charrey.algorithms.RefuseLongerPaths;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.occupation.ReadOnlyOccupation;
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
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.function.Consumer;
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
     * @param transaction             the occupationTransaction where intermediate nodes are registered
     * @param initialLocalOccupation a set used to keep track of vertices used in this very path
     * @param controlPoints          the number of control points that should be used
     * @param verticesPlaced         a supplier of the number of source graph vertices currently matched
     * @param settings               the settings of the homeomorphism search
     */
    ControlPointIterator(@NotNull MyGraph targetGraph,
                         int tail,
                         int head,
                         @NotNull GlobalOccupation globalOccupation,
                         @NotNull OccupationTransaction transaction,
                         @NotNull TIntSet initialLocalOccupation,
                         int controlPoints,
                         Supplier<Integer> verticesPlaced,
                         Settings settings,
                         PartialMatchingProvider provider,
                         long timeoutTime) {
        super(targetGraph, tail, head, settings, globalOccupation, transaction, provider, timeoutTime, verticesPlaced, 0);
        this.targetGraph = targetGraph;
        this.controlPoints = controlPoints;
        this.globalOccupation = globalOccupation;
        this.localOccupation = initialLocalOccupation;
        this.head = head;
        this.controlPointCandidates = new ControlPointVertexSelector(targetGraph, transaction, TCollections.unmodifiableSet(initialLocalOccupation), tail, head, settings.getRefuseLongerPaths(), tail);
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
        return Util.filteredShortestPath(targetGraph, transaction, localOccupation, from, to, settings.getRefuseLongerPaths(), tail(), Util.emptyTIntSet);
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
                temporarilyRemoveGlobal.forEach(x -> {
                    if (transaction.isOccupiedRouting(x)) {
                        transaction.releaseRouting(verticesPlaced.get(), x, this::getPartialMatching);
                    }
                });
                Optional<Path> leftToMiddleAlt = Util.filteredShortestPath(targetGraph, transaction, fictionalOccupation, leftCandidate, middleAlt, settings.getRefuseLongerPaths(), tail(), Util.emptyTIntSet);
                if (leftToMiddleAlt.isEmpty()) {
                    temporarilyRemoveGlobal.asList().forEach(x -> {
                        try {
                            transaction.occupyRoutingAndCheck(verticesPlaced.get(), x, this::getPartialMatching);
                        } catch (DomainCheckerException ignored) {

                        }
                        return true;
                    });
                    return true;
                }
                Path leftToRightAlt = Util.merge(targetGraph, leftToMiddleAlt.get(), middleAltToRight);
                Optional<Path> leftToMiddle = filteredShortestPath(leftCandidate, middle);
                final boolean[] failed = {false};
                temporarilyRemoveGlobal.forEachReverse(x -> {
                    try {
                        transaction.occupyRoutingAndCheck(verticesPlaced.get(), x, this::getPartialMatching);
                    } catch (DomainCheckerException e) {
                        failed[0] = true;
                    }
                });
                if (failed[0] || leftToMiddle.isEmpty()) {
                    return true;
                }
                Path leftToRight = Util.merge(targetGraph, leftToMiddle.get(), middleToRight);
                if (leftToRight.isEqualTo(leftToRightAlt)) {
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
            temporaryRemoveFromGlobal.forEach(x -> {
                if (transaction.isOccupiedRouting(x)) {
                    transaction.releaseRouting(verticesPlaced.get(), x, this::getPartialMatching);
                }
            });
            Optional<Path> skippedPath = Util.filteredShortestPath(targetGraph, transaction, localOccupation, left, right, settings.getRefuseLongerPaths(), tail(), Util.emptyTIntSet);

           if (!temporaryRemoveFromGlobal.asList().forEach(x -> {
                try {
                    transaction.occupyRoutingAndCheck(verticesPlaced.get(), x, this::getPartialMatching);
                    return true;
                } catch (DomainCheckerException e) {
                    return false;
                }
            })) {
               temporaryRemoveFromGlobal.forEach(x -> {
                   if (transaction.isOccupiedRouting(x)) {
                       transaction.releaseRouting(verticesPlaced.get(), x, this::getPartialMatching);
                   }
               });
           }
            this.pathFromRightNeighbourToItsRightNeighbour.forEach(localOccupation::add);
            return skippedPath.map(integers -> integers.contains(middle)).orElse(true);
        }
    }

    private boolean tryOccupy(int verticesPlaced, Path path) {
        try {
            transaction.occupyRoutingAndCheck(verticesPlaced, path, this::getPartialMatching);
            return true;
        } catch (DomainCheckerException e) {
            return false;
        }
    }

    private int findNextControlPoint() {
        do {
            chosenPath = null;
            chosenControlPoint = controlPointCandidates.next();
        } while (chosenControlPoint != ABSENT && (makesNeighbourUseless(chosenControlPoint) || rightShiftPossible(chosenControlPoint)));
        if (chosenControlPoint == ABSENT) {
            done = true;
            return EXHAUSTED;
        }
        try {
            this.transaction.occupyRoutingAndCheck(verticesPlaced.get(), chosenControlPoint, this::getPartialMatching);
        } catch (DomainCheckerException e) {
            chosenControlPoint = ControlPointIterator.ABSENT;
            return findNextControlPoint();
        }
        return 0;
    }

    @Override
    public TIntSet getLocallyOccupied() {
        TIntSet res = new TIntHashSet();
        if (this.chosenControlPoint != ABSENT) {
            res.add(chosenControlPoint);
        }
        if (this.chosenPath != null) {
            res.addAll(chosenPath.intermediate().asList());
        }
        if (child != null) {
            res.addAll(child.getLocallyOccupied());
        }
        return res;
    }

    @Nullable
    @Override
    public Path getNext() {
        if (done || Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= timeoutTime) {
            return cleanUp();
        }
        while (!done) {
            if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= timeoutTime) {
                return null;
            }
            if (controlPoints == 0) {
                return provideShortestPath();
            } else if (child == null) {
                releasePreviousControlPoint();
                if (!transaction.isFruitful(verticesPlaced.get(), this::getPartialMatching, -1) || findNextControlPoint() == EXHAUSTED) {
                    return null;
                }
                Optional<Path> graphPath = filteredShortestPath(chosenControlPoint, head);
                if (graphPath.isPresent()) {
                    chosenPath = graphPath.get();
                    try {
                        transaction.occupyRoutingAndCheck(verticesPlaced.get(), chosenPath, this::getPartialMatching);
                    } catch (DomainCheckerException e) {
                        continue;
                    }
                    TIntSet previousOccupation = new TIntHashSet(localOccupation);
                    chosenPath.forEach(localOccupation::add);
                    child = new ControlPointIterator(targetGraph, tail(), chosenControlPoint, globalOccupation, transaction, new TIntHashSet(localOccupation), controlPoints - 1, verticesPlaced, settings, partialMatchingProvider, timeoutTime);
                    child.setRightNeighbourOfRightNeighbour(this.head(), chosenPath, previousOccupation);
                }
            } else {
                Path toReturn = mergeWithChildsPath();
                if (toReturn != null) {
                    return toReturn;
                }
            }
        }
        return cleanUp();
    }

    private Path cleanUp() {
        if (controlPoints == 0 && chosenPath != null) {
            if (chosenPath.length() > 2) {
                chosenPath.intermediate().forEach(x -> transaction.releaseRouting(verticesPlaced.get(), x, this::getPartialMatching));
            }
            chosenPath = null;
        }
        return null;
    }

    private Optional<Path> findIntersectedPath(int from, int to, ReadOnlyOccupation occupation) {
        Set<Integer> explored = new HashSet<>();
        Deque<Pair<Integer, Path>> frontier = new LinkedList<>();
        Graphs.successorListOf(targetGraph, from)
                .stream()
                .filter(x -> !occupation.isOccupied(x) && (x == to || (targetGraph.inDegreeOf(x) == 1 && targetGraph.outDegreeOf(x) == 1)))
                .forEach(x -> frontier.add(new Pair<>(x, new Path(targetGraph, List.of(from, x)))));
        while (!frontier.isEmpty()) {
            Pair<Integer, Path> considering = frontier.pollFirst();
            explored.add(considering.getFirst());
            if (to == considering.getFirst()) {
                assert considering.getSecond() != null;
                return Optional.of(considering.getSecond());
            } else if (Graphs.successorListOf(targetGraph, considering.getFirst()).contains(to)) {
                Path res = considering.getSecond().append(to);
                assert res != null;
                return Optional.of(res);
            } else if (Graphs.successorListOf(targetGraph, considering.getFirst()).size() == 1 && Graphs.predecessorListOf(targetGraph, considering.getFirst()).size() == 1) {
                int successor = Graphs.successorListOf(targetGraph, considering.getFirst()).iterator().next();
                if (!explored.contains(successor) &&
                        !occupation.isOccupied(successor) &&
                        (targetGraph.incomingEdgesOf(successor).size() == 1 && targetGraph.outgoingEdgesOf(successor).size() == 1)) {
                    frontier.addLast(new Pair<>(successor, considering.getSecond().append(successor)));
                }
            }
        }
        return Optional.empty();
    }

    private void releasePreviousControlPoint() {
        if (chosenControlPoint != ABSENT && this.transaction.isOccupiedRouting(chosenControlPoint)) {
            this.transaction.releaseRouting(verticesPlaced.get(), chosenControlPoint, this::getPartialMatching);
        }
    }

    @Nullable
    private Path mergeWithChildsPath() {
        TIntSet localOccupationBackup = new TIntHashSet(localOccupation);
        assert child != null;
        Path childsPath = child.next();
        assert childsPath == null || childsPath.intermediate().noneMatch(localOccupationBackup::contains);
        assert chosenPath != null;
        if (childsPath != null) {
            return Util.merge(targetGraph, childsPath, chosenPath);
        } else {
            child = null;
            chosenPath.forEach(localOccupation::remove);
            chosenPath.intermediate().forEach(x -> {
                if (transaction.isOccupiedRouting(x)) {
                    transaction.releaseRouting(verticesPlaced.get(), x, this::getPartialMatching);
                }
            });
            return null;
        }
    }

    @Nullable
    private Path provideShortestPath() {
        done = true;
        Optional<Path> shortestPath = filteredShortestPath(tail(), head);
        if (shortestPath.isEmpty() || !tryOccupy(verticesPlaced.get(), shortestPath.get())) {
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
