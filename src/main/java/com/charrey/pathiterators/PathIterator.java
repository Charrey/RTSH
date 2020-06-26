package com.charrey.pathiterators;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.DFSPathIterator;
import com.charrey.pathiterators.kpath.KPathPathIterator;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.charrey.settings.PathIterationConstants.*;

/**
 * The type Path iterator.
 */
public abstract class PathIterator {


    private final int head;
    private final int tail;
    /**
     * The Refuse longer paths.
     */
    protected final boolean refuseLongerPaths;

    /**
     * Instantiates a new Path iterator.
     *
     * @param tail              the tail
     * @param head              the head
     * @param refuseLongerPaths the refuse longer paths
     */
    protected PathIterator(int tail, int head, boolean refuseLongerPaths) {
        this.tail = tail;
        this.head = head;
        this.refuseLongerPaths = refuseLongerPaths;
    }

    /**
     * Get path iterator.
     *
     * @param targetGraph   the target graph
     * @param data          the data
     * @param tail          the tail
     * @param head          the head
     * @param occupation    the occupation
     * @param placementSize the placement size
     * @param settings      the settings
     * @return the path iterator
     */
    @NotNull
    public static PathIterator get(@NotNull MyGraph targetGraph, @NotNull UtilityData data, int tail, int head, @NotNull GlobalOccupation occupation, Supplier<Integer> placementSize, @NotNull Settings settings) {
        return get(targetGraph, data, tail, head, occupation, placementSize, settings.pathIteration.iterationStrategy, settings.refuseLongerPaths);
    }

    /**
     * Get path iterator.
     *
     * @param targetGraph       the target graph
     * @param data              the data
     * @param tail              the tail
     * @param head              the head
     * @param occupation        the occupation
     * @param placementSize     the placement size
     * @param pathIteration     the path iteration
     * @param refuseLongerPaths the refuse longer paths
     * @return the path iterator
     */
    @NotNull
    public static PathIterator get(@NotNull MyGraph targetGraph, @NotNull UtilityData data, int tail, int head, @NotNull GlobalOccupation occupation, Supplier<Integer> placementSize, int pathIteration, boolean refuseLongerPaths) {
        if (targetGraph.getEdge(tail, head) != null) {
            return new SingletonPathIterator(targetGraph, tail, head);
        }

        switch (pathIteration) {
            case DFS_ARBITRARY:
            case DFS_GREEDY:
                int[][] targetNeighbours = data.getTargetNeighbours(pathIteration)[head];
                return new DFSPathIterator(targetGraph, targetNeighbours, tail, head, occupation, placementSize, refuseLongerPaths);
            case CONTROL_POINT:
                return new ManagedControlPointIterator(targetGraph, tail, head, occupation, 1, placementSize, refuseLongerPaths);
            case KPATH:
                return new KPathPathIterator(targetGraph, tail, head, occupation, placementSize, refuseLongerPaths);
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Next path.
     *
     * @return the path
     */
    @Nullable
    public abstract Path next();

    /**
     * Tail int.
     *
     * @return the int
     */
    public int tail() {
        return tail;
    }

    /**
     * Head int.
     *
     * @return the int
     */
    public int head() {
        return head;
    }

    /**
     * Debug info string.
     *
     * @return the string
     */
    public abstract String debugInfo();
}
