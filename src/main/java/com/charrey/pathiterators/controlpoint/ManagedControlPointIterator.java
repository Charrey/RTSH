package com.charrey.pathiterators.controlpoint;

import com.charrey.occupation.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.PathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
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
    private final Occupation globalOccupation;
    private final OccupationTransaction transaction;
    private final int maxControlPoints;
    private final Supplier<Integer> verticesPlaced;
    private ControlPointIterator child;
    private int controlPoints = 0;

    public ManagedControlPointIterator(@NotNull MyGraph graph, @NotNull Vertex tail, @NotNull Vertex head, @NotNull Occupation globalOccupation, int maxControlPoints, Supplier<Integer> verticesPlaced, boolean refuseLongerPaths) {
        super(tail, head, refuseLongerPaths);
        child = new ControlPointIterator(graph, tail, head, globalOccupation, new HashSet<>(), controlPoints, verticesPlaced, refuseLongerPaths);
        this.graph = graph;
        this.globalOccupation = globalOccupation;
        this.transaction = globalOccupation.getTransaction();
        this.maxControlPoints = maxControlPoints;
        this.verticesPlaced = verticesPlaced;
    }

    @Nullable
    private Path returned = null;

    @Nullable
    @Override
    public Path next() {
        transaction.uncommit();
        if (returned != null) {
            returned.intermediate().forEach(x -> transaction.releaseRouting(verticesPlaced.get(), x));
        }
        while (true) {
            Path path;
            do {
                path = child.next();
                System.out.print("");
            } while (path != null && controlPoints > 0 && (makesLastControlPointUseless() || rightShiftPossible()));
            if (path != null) {
                try {
                    transaction.occupyRoutingAndCheck(verticesPlaced.get(), path);
                } catch (DomainCheckerException e) {
                    continue;
                }
                returned = path;
                transaction.commit();
                return returned;
            } else {
                if (controlPoints + 1 > maxControlPoints || controlPoints + 1 > graph.vertexSet().size() - 2) {
                    return null;
                }
                controlPoints += 1;
                child = new ControlPointIterator(graph, tail(), head(), globalOccupation, new HashSet<>(), controlPoints, verticesPlaced, refuseLongerPaths);
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
            Path leftToMiddleAlt = ControlPointIterator.filteredShortestPath(graph, globalOccupation, fictionalLocalOccupation, left, middleAlt);
            assert leftToMiddleAlt != null;
            Path alternative = ControlPointIterator.merge(leftToMiddleAlt, middleAltToRight);
            if (alternative.equals(leftToRight)) {
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
        Path skippedPath = ControlPointIterator.filteredShortestPath(graph, globalOccupation, localOccupations.get(1), left, right);
        assert skippedPath != null;
        assert skippedPath.tail() == left;
        assert skippedPath.head() == right;
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


    @NotNull
    public List<Vertex> controlPoints() {
        return child.controlPoints();
    }

}
