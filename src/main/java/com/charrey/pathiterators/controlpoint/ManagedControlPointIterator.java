package com.charrey.pathiterators.controlpoint;

import com.charrey.algorithms.RefuseLongerPaths;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.settings.Settings;
import com.charrey.util.Util;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * A path iterator that iterates over paths by adding intermediate vertices that must be visited before the end vertex.
 * A shortest path algorithm is then applied to obtain the resulting path. The number of intermediate vertices increases
 * the more often next() is called.
 */
public class ManagedControlPointIterator extends PathIterator {


    @NotNull
    private final MyGraph graph;
    @NotNull
    private final GlobalOccupation globalOccupation;
    private final int maxControlPoints;
    private final Supplier<Integer> verticesPlaced;
    private static final Logger LOG = Logger.getLogger("ManagedControlPointIterator");
    private ControlPointIterator child;
    private int numberOfControlPoints = 0;
    private final Settings settings;

    /**
     * Instantiates a new ManagedControlPointIterator.
     *
     * @param graph            the target graph
     * @param tail             the source of the path
     * @param head             the target of the path
     * @param globalOccupation the GlobalOccupation where intermediate nodes are registered
     * @param verticesPlaced   supplier of the number of source graph vertices placed at this point in the search
     * @param maxControlPoints the maximum number of control points used (lower this for 'simpler' paths)
     */
    public ManagedControlPointIterator(@NotNull MyGraph graph,
                                       Settings settings, int tail,
                                       int head,
                                       @NotNull GlobalOccupation globalOccupation,
                                       Supplier<Integer> verticesPlaced,
                                       PartialMatchingProvider provider,
                                       int maxControlPoints,
                                       long timeoutTime, int cripple) {
        super(graph, tail, head, settings, globalOccupation, globalOccupation.getTransaction(), provider, timeoutTime, verticesPlaced, cripple);
        this.graph = graph;
        this.globalOccupation = globalOccupation;
        this.settings = settings;
        this.child = new ControlPointIterator(graph, tail, head, globalOccupation, transaction, new TIntHashSet(), numberOfControlPoints, verticesPlaced, settings, provider, timeoutTime);
        this.maxControlPoints = maxControlPoints;
        this.verticesPlaced = verticesPlaced;
    }

    @Override
    public TIntSet getLocallyOccupied() {
        return child == null ? Util.emptyTIntSet : child.getLocallyOccupied();
    }

    @Nullable
    @Override
    public Path getNext() {
        transaction.uncommit(verticesPlaced.get(), this::getPartialMatching);
        while (true) {
            Path path;
            do {
                if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= timeoutTime) {
                    return null;
                }
                path = child.next();
            } while (path != null && numberOfControlPoints > 0 && (makesLastControlPointUseless() || rightShiftPossible() || (refuseLongerPaths && RefuseLongerPaths.hasUnnecessarilyLongPaths(graph, path))));
            if (path != null) {
                try {
                    transaction.commit(verticesPlaced.get(), this::getPartialMatching);
                } catch (DomainCheckerException e) {
                    System.out.println("Failed to commit");
                    continue;
                }
                Path finalPath = path;
                LOG.finest(() -> "ManagedControlPointIterator returned path " + finalPath);
                return path;
            } else {
                if (numberOfControlPoints + 1 > maxControlPoints || numberOfControlPoints + 1 > graph.vertexSet().size() - 2) {
                    return null;
                }
                numberOfControlPoints += 1;
                LOG.finest(() -> "Raising control point count to " + numberOfControlPoints);
                TIntSet localOccupation = new TIntHashSet();
                localOccupation.add(head());
                child = new ControlPointIterator(graph, tail(), head(), globalOccupation, transaction, localOccupation, numberOfControlPoints, verticesPlaced, settings, partialMatchingProvider, timeoutTime);
            }
        }
    }

    @Override
    public String debugInfo() {
        return "controlpoints(" + controlPoints() + ")";
    }

    private boolean rightShiftPossible() {
        int left = tail();
        if (controlPoints().size() == 0) {
            return false;
        }
        List<Path> intermediatePaths = intermediatePaths();
        List<TIntSet> localOccupations = localOccupations();
        Path leftToMiddle = intermediatePaths.get(0);
        Path middleToRight = intermediatePaths.get(1);
        Path leftToRight = Util.merge(graph, leftToMiddle, middleToRight);

        for (int i = 0; i < middleToRight.intermediate().length(); i++){
            int middleAlt = middleToRight.intermediate().get(i);
            Path middleAltToRight = new Path(graph, middleToRight.asList().subList(i + 1, middleToRight.length()));
            TIntSet fictionalLocalOccupation = new TIntHashSet(localOccupations.get(1));
            middleAltToRight.forEach(fictionalLocalOccupation::add);
            Optional<Path> leftToMiddleAlt = Util.filteredShortestPath(graph, globalOccupation, fictionalLocalOccupation, left, middleAlt, refuseLongerPaths, tail(), Util.emptyTIntSet);
            assert leftToMiddleAlt.isPresent();
            Path alternative = Util.merge(graph, leftToMiddleAlt.get(), middleAltToRight);
            if (alternative.isEqualTo(leftToRight)) {
                LOG.finest(() -> "Right-shift possible to vertex " + middleAlt);
                return true;
            }
        }
        return false;
    }

    private boolean makesLastControlPointUseless() {
        List<Integer> controlPoints = controlPoints();
        if (controlPoints.size() == 0) {
            return false;
        }
        int left = tail();
        int middle = controlPoints.get(0);
        int right = controlPoints.size() > 1 ? controlPoints.get(1) : head();

        List<Path> intermediatePaths = intermediatePaths();
        List<TIntSet> localOccupations = localOccupations();
        Path middleToRight = intermediatePaths.get(1);
        Path leftToRight = Util.merge(graph, intermediatePaths.get(0), intermediatePaths.get(1));

        assert middleToRight.first() == middle;
        assert middleToRight.last() == right;
        Optional<Path> skippedPath = Util.filteredShortestPath(graph, globalOccupation, localOccupations.get(1), left, right, refuseLongerPaths, tail(), Util.emptyTIntSet);
        assert skippedPath.isPresent();
        assert skippedPath.get().first() == left;
        assert skippedPath.get().last() == right;
        if (skippedPath.get().isEqualTo(leftToRight)) {
            LOG.finest("Makes last control point useless...");
        }
        return skippedPath.get().isEqualTo(leftToRight);
    }

    @NotNull
    private List<Path> intermediatePaths() {
        ControlPointIterator current = child;
        List<Path> res = new LinkedList<>();
        while (current != null) {
            res.add(0, current.getChosenPath());
            current = current.getChild();
        }
        return res;
    }

    @NotNull
    private List<TIntSet> localOccupations() {
        ControlPointIterator current = child;
        List<TIntSet> res = new LinkedList<>();
        while (current != null) {
            res.add(0, current.getLocalOccupation());
            current = current.getChild();
        }
        res.add(new TIntHashSet());
        res.remove(0);
        return res;
    }

    /**
     * Returns the last path returned by this path iterator.
     *
     * @return the last path returned
     */
    public Path finalPath() {
        if (child == null) {
            return null;
        } else {
            return child.finalPath();
        }
    }

    /**
     * Returns the first path segment (up to the first control point) of the last returned path
     *
     * @return the first path segment
     */
    public Path firstPath() {
        if (child == null) {
            return null;
        } else {
            return child.getChosenPath();
        }
    }


    /**
     * Returns a list of control points from left (source) to right (target).
     *
     * @return the control points
     */
    @NotNull
    public List<Integer> controlPoints() {
        return child.controlPoints();
    }

}
