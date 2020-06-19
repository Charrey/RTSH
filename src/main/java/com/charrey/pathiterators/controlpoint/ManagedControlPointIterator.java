package com.charrey.pathiterators.controlpoint;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.PathIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ManagedControlPointIterator extends PathIterator {


    @NotNull
    private final MyGraph graph;
    @NotNull
    private final GlobalOccupation globalOccupation;
    private final OccupationTransaction transaction;
    private final int maxControlPoints;
    private final Supplier<Integer> verticesPlaced;
    private ControlPointIterator child;
    private int controlPoints = 0;

    public ManagedControlPointIterator(@NotNull MyGraph graph, @NotNull Vertex tail, @NotNull Vertex head, @NotNull GlobalOccupation globalOccupation, int maxControlPoints, Supplier<Integer> verticesPlaced, boolean refuseLongerPaths) {
        super(tail, head, refuseLongerPaths);
        this.graph = graph;
        this.globalOccupation = globalOccupation;
        this.transaction = globalOccupation.getTransaction();
        this.child = new ControlPointIterator(graph, tail, head, transaction, new HashSet<>(), controlPoints, verticesPlaced, refuseLongerPaths);
        this.maxControlPoints = maxControlPoints;
        this.verticesPlaced = verticesPlaced;
    }

    @Nullable
    private Path returned = null;

    @Nullable
    @Override
    public Path next() {
        transaction.uncommit();
        while (true) {
            Path path;
            do {
                path = child.next();
            } while (path != null && controlPoints > 0 && (makesLastControlPointUseless() || rightShiftPossible()));
            if (path != null) {
                returned = path;
                transaction.commit();
                return returned;
            } else {
                if (controlPoints + 1 > maxControlPoints || controlPoints + 1 > graph.vertexSet().size() - 2) {
                    return null;
                }
                controlPoints += 1;
                if (ControlPointIterator.log) {
                    System.out.println("Raising control point count to " + controlPoints);
                }
                Set<Integer> localOccupation = new HashSet<>();
                localOccupation.add(head().data());
                child = new ControlPointIterator(graph, tail(), head(), transaction, localOccupation, controlPoints, verticesPlaced, refuseLongerPaths);
            }
        }
    }

    private boolean rightShiftPossible() {
        Vertex left = tail();
        List<Path> intermediatePaths = intermediatePaths();
        List<Set<Integer>> localOccupations = localOccupations();
        Path leftToMiddle = intermediatePaths.get(0);
        Path middleToRight = intermediatePaths.get(1);
        Path leftToRight = ControlPointIterator.merge(leftToMiddle, middleToRight);

        for (int i = 0; i < middleToRight.intermediate().size(); i++){
            Vertex middleAlt = middleToRight.intermediate().get(i);
            Path middleAltToRight = new Path(middleToRight.asList().subList(i + 1, middleToRight.length()));
            Set<Integer> fictionalLocalOccupation = new HashSet<>(localOccupations.get(1));
            middleAltToRight.forEach(x -> fictionalLocalOccupation.add(x.data()));
            Path leftToMiddleAlt = ControlPointIterator.filteredShortestPath(graph, globalOccupation, fictionalLocalOccupation, left, middleAlt, refuseLongerPaths, tail());
            assert leftToMiddleAlt != null;
            Path alternative = ControlPointIterator.merge(leftToMiddleAlt, middleAltToRight);
            if (alternative.equals(leftToRight)) {
                if (ControlPointIterator.log) {
                    System.out.println("Right-shift possible to vertex " + middleAlt);
                }
                return true;
            }
        }
        return false;
    }

    private boolean makesLastControlPointUseless() {
        List<Vertex> controlPoints = controlPoints();
        Vertex left = tail();
        Vertex middle = controlPoints.get(0);
        Vertex right = controlPoints.size() > 1 ? controlPoints.get(1) : head();

        List<Path> intermediatePaths = intermediatePaths();
        List<Set<Integer>> localOccupations = localOccupations();
        Path middleToRight = intermediatePaths.get(1);
        Path leftToRight = ControlPointIterator.merge(intermediatePaths.get(0), intermediatePaths.get(1));

        assert middleToRight.tail() == middle;
        assert middleToRight.head() == right;
        Path skippedPath = ControlPointIterator.filteredShortestPath(graph, globalOccupation, localOccupations.get(1), left, right, refuseLongerPaths, tail());
        assert skippedPath != null;
        assert skippedPath.tail() == left;
        assert skippedPath.head() == right;
        if (skippedPath.equals(leftToRight) && ControlPointIterator.log) {
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
    private List<Set<Integer>> localOccupations() {
        ControlPointIterator current = child;
        List<Set<Integer>> res = new LinkedList<>();
        while (current != null) {
            res.add(0, current.getLocalOccupation());
            current = current.getChild();
        }
        res.add(new HashSet<>());
        res.remove(0);
        return res;
    }

    public Path finalPath() {
        if (child == null) {
            return null;
        } else {
            return child.finalPath();
        }
    }

    public Path firstPath() {
        if (child == null) {
            return null;
        } else {
            return child.getChosenPath();
        }
    }


    @NotNull
    public List<Vertex> controlPoints() {
        return child.controlPoints();
    }

}
