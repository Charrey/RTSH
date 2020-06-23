package com.charrey.pathiterators;

import com.charrey.occupation.GlobalOccupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.DFSPathIterator;
import com.charrey.pathiterators.yen.YenPathIterator;
import com.charrey.settings.Settings;
import com.charrey.algorithms.UtilityData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.charrey.settings.PathIterationStrategy.*;

public abstract class PathIterator {


    private final Vertex head;
    private final Vertex tail;
    protected final boolean refuseLongerPaths;

    protected PathIterator(Vertex tail, Vertex head, boolean refuseLongerPaths) {
        this.tail = tail;
        this.head = head;
        this.refuseLongerPaths = refuseLongerPaths;
    }

    @NotNull
    public static PathIterator get(@NotNull MyGraph targetGraph, @NotNull UtilityData data, @NotNull Vertex tail, @NotNull Vertex head, @NotNull GlobalOccupation occupation, Supplier<Integer> placementSize, @NotNull Settings settings) {
        return get(targetGraph, data, tail, head, occupation, placementSize, settings.pathIteration, settings.refuseLongerPaths);
    }

    @NotNull
    public static PathIterator get(@NotNull MyGraph targetGraph, @NotNull UtilityData data, @NotNull Vertex tail, @NotNull Vertex head, @NotNull GlobalOccupation occupation, Supplier<Integer> placementSize, int pathIteration, boolean refuseLongerPaths) {
        if (targetGraph.getEdge(tail, head) != null) {
            return new SingletonPathIterator(targetGraph, tail, head);
        }

        switch (pathIteration) {
            case DFS_ARBITRARY:
            case DFS_GREEDY:
                Vertex[][] targetNeighbours = data.getTargetNeighbours(pathIteration)[head.data()];
                return new DFSPathIterator(targetGraph, targetNeighbours, tail, head, occupation, placementSize, refuseLongerPaths);
            case CONTROL_POINT:
                return new ManagedControlPointIterator(targetGraph, tail, head, occupation, 300, placementSize, refuseLongerPaths);
            case YEN:
                return new YenPathIterator(targetGraph, tail, head, occupation, placementSize, refuseLongerPaths);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Nullable
    public abstract Path next();

    public Vertex tail() {
        return tail;
    }

    public Vertex head() {
        return head;
    }

    public abstract String debugInfo();
}
