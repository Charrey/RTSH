package com.charrey.pathiterators.controlpoint;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pruning.DomainCheckerException;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

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
    private final ControlPointIteratorRelevantSettings settings;
    private ControlPointIterator child;
    private int controlPoints = 0;

    /**
     * Instantiates a new ManagedControlPointIterator.
     *
     * @param graph             the target graph
     * @param tail              the source of the path
     * @param head              the target of the path
     * @param globalOccupation  the GlobalOccupation where intermediate nodes are registered
     * @param maxControlPoints  the maximum number of control points used (lower this for 'simpler' paths)
     * @param verticesPlaced    supplier of the number of source graph vertices placed at this point in the search
     * @param refuseLongerPaths whether to refuse paths that use unnecessarily many resources.
     */
    public ManagedControlPointIterator(@NotNull MyGraph graph,
                                       int tail,
                                       int head,
                                       @NotNull GlobalOccupation globalOccupation,
                                       int maxControlPoints,
                                       Supplier<Integer> verticesPlaced,
                                       boolean refuseLongerPaths,
                                       PartialMatchingProvider provider) {
        super(tail, head, refuseLongerPaths, globalOccupation.getTransaction(), provider);
        this.graph = graph;
        this.globalOccupation = globalOccupation;
        this.settings = new ControlPointIteratorRelevantSettings(true);
        this.child = new ControlPointIterator(graph, tail, head, transaction, new TIntHashSet(), controlPoints, verticesPlaced, settings, provider);
        this.maxControlPoints = maxControlPoints;
        this.verticesPlaced = verticesPlaced;
    }

    @Nullable
    @Override
    public Path next() {
        transaction.uncommit(verticesPlaced.get());
        while (true) {
            Path path;
            do {
                path = child.next();
            } while (path != null && controlPoints > 0 && (makesLastControlPointUseless() || rightShiftPossible()));
            if (path != null) {
                try {
                    transaction.commit(verticesPlaced.get(), getPartialMatching());
                } catch (DomainCheckerException e) {
                    continue;
                }
                if (settings.log) {
                    System.out.println("ManagedControlPointIterator returned path " + path);
                }
                return path;
            } else {
                if (controlPoints + 1 > maxControlPoints || controlPoints + 1 > graph.vertexSet().size() - 2) {
                    return null;
                }
                controlPoints += 1;
                if (settings.log) {
                    System.out.println("Raising control point count to " + controlPoints);
                }
                TIntSet localOccupation = new TIntHashSet();
                localOccupation.add(head());
                child = new ControlPointIterator(graph, tail(), head(), transaction, localOccupation, controlPoints, verticesPlaced, settings, partialMatchingProvider);
            }
        }
    }

    @Override
    public String debugInfo() {
        return "controlpoints(" + controlPoints + ")";
    }

    private boolean rightShiftPossible() {
        int left = tail();
        List<Path> intermediatePaths = intermediatePaths();
        List<TIntSet> localOccupations = localOccupations();
        Path leftToMiddle = intermediatePaths.get(0);
        Path middleToRight = intermediatePaths.get(1);
        Path leftToRight = ControlPointIterator.merge(graph, leftToMiddle, middleToRight);

        for (int i = 0; i < middleToRight.intermediate().size(); i++){
            int middleAlt = middleToRight.intermediate().get(i);
            Path middleAltToRight = new Path(graph, middleToRight.asList().subList(i + 1, middleToRight.length()));
            TIntSet fictionalLocalOccupation = new TIntHashSet(localOccupations.get(1));
            middleAltToRight.forEach(fictionalLocalOccupation::add);
            Path leftToMiddleAlt = ControlPointIterator.filteredShortestPath(graph, globalOccupation, fictionalLocalOccupation, left, middleAlt, refuseLongerPaths, tail());
            assert leftToMiddleAlt != null;
            Path alternative = ControlPointIterator.merge(graph, leftToMiddleAlt, middleAltToRight);
            if (alternative.equals(leftToRight)) {
                if (settings.log) {
                    System.out.println("Right-shift possible to vertex " + middleAlt);
                }
                return true;
            }
        }
        return false;
    }

    private boolean makesLastControlPointUseless() {
        List<Integer> controlPoints = controlPoints();
        int left = tail();
        int middle = controlPoints.get(0);
        int right = controlPoints.size() > 1 ? controlPoints.get(1) : head();

        List<Path> intermediatePaths = intermediatePaths();
        List<TIntSet> localOccupations = localOccupations();
        Path middleToRight = intermediatePaths.get(1);
        Path leftToRight = ControlPointIterator.merge(graph, intermediatePaths.get(0), intermediatePaths.get(1));

        assert middleToRight.first() == middle;
        assert middleToRight.last() == right;
        Path skippedPath = ControlPointIterator.filteredShortestPath(graph, globalOccupation, localOccupations.get(1), left, right, refuseLongerPaths, tail());
        assert skippedPath != null;
        assert skippedPath.first() == left;
        assert skippedPath.last() == right;
        if (skippedPath.equals(leftToRight) && settings.log) {
            System.out.println("Makes last control point useless...");
        }
        return skippedPath.equals(leftToRight);
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
