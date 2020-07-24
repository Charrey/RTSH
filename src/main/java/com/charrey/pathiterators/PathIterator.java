package com.charrey.pathiterators;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.DFSPathIterator;
import com.charrey.pathiterators.kpath.KPathPathIterator;
import com.charrey.pruning.PartialMatching;
import com.charrey.settings.Settings;
import com.charrey.settings.iterator.ControlPointIteratorStrategy;
import com.charrey.settings.iterator.IteratorSettings;
import com.charrey.settings.pruning.PruningApplicationConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.charrey.settings.PathIterationConstants.*;

/**
 * The type Path iterator.
 */
public abstract class PathIterator {


    private final int maxPaths = Integer.MAX_VALUE;
    private final GlobalOccupation globalOccupation;


    private final int head;
    private final int tail;
    /**
     * The Refuse longer paths.
     */
    protected final boolean refuseLongerPaths;
    private int counter = 0;
    protected PartialMatchingProvider partialMatchingProvider;
    protected OccupationTransaction transaction;


    /**
     * Instantiates a new Path iterator.
     *
     * @param tail              the tail
     * @param head              the head
     * @param refuseLongerPaths the refuse longer paths
     */
    protected PathIterator(int tail, int head, boolean refuseLongerPaths, GlobalOccupation globalOccupation, OccupationTransaction transaction, PartialMatchingProvider partialMatchingProvider) {
        this.tail = tail;
        this.head = head;
        this.refuseLongerPaths = refuseLongerPaths;
        this.partialMatchingProvider = partialMatchingProvider;
        this.globalOccupation = globalOccupation;
        this.transaction = transaction;
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
    public static PathIterator get(@NotNull MyGraph targetGraph,
                                   @NotNull UtilityData data,
                                   int tail,
                                   int head,
                                   @NotNull GlobalOccupation occupation,
                                   Supplier<Integer> placementSize,
                                   @NotNull Settings settings,
                                   PartialMatchingProvider provider) {
        return get(targetGraph, data, tail, head, occupation, placementSize, settings.pathIteration, settings.refuseLongerPaths, provider, settings.whenToApply);
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
    public static PathIterator get(@NotNull MyGraph targetGraph,
                                   @NotNull UtilityData data,
                                   int tail,
                                   int head,
                                   @NotNull GlobalOccupation occupation,
                                   Supplier<Integer> placementSize,
                                   IteratorSettings pathIteration,
                                   boolean refuseLongerPaths,
                                   PartialMatchingProvider provider,
                                   PruningApplicationConstants whenToApply) {
        if (targetGraph.getEdge(tail, head) != null) {
            return new SingletonPathIterator(targetGraph, tail, head, provider);
        }
        switch (pathIteration.iterationStrategy) {
            case DFS_ARBITRARY:
            case DFS_GREEDY:
                int[][] targetNeighbours = data.getTargetNeighbours(pathIteration.iterationStrategy)[head];
                return new DFSPathIterator(targetGraph, targetNeighbours, tail, head, occupation, placementSize, refuseLongerPaths, provider);
            case CONTROL_POINT:
                return new ManagedControlPointIterator(targetGraph, tail, head, occupation, ((ControlPointIteratorStrategy) pathIteration).getMaxControlpoints(), placementSize, refuseLongerPaths, provider);
            case KPATH:
                return new KPathPathIterator(targetGraph, tail, head, occupation, placementSize, refuseLongerPaths, provider, whenToApply == PruningApplicationConstants.CACHED);
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected PartialMatching getPartialMatching() {
        PartialMatching fromParent = partialMatchingProvider.getPartialMatching();
        return new PartialMatching(fromParent.getVertexMapping(), fromParent.getEdgeMapping(), transaction.getLocallyOccupied());
    }


    @Nullable
    public abstract Path getNext();

    @Nullable
    public Path next() {
        if (counter == maxPaths) {
            return null;
        }
        Path toReturn;
        if (globalOccupation != null) {
            globalOccupation.claimActiveOccupation(transaction);
            toReturn = getNext();
            globalOccupation.unclaimActiveOccupation(transaction);
        } else {
            toReturn = getNext();
        }
        counter++;
        return toReturn;
    }

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
