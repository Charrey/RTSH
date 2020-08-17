package com.charrey.pathiterators;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.CachedDFSPathIterator;
import com.charrey.pathiterators.dfs.InPlaceDFSPathIterator;
import com.charrey.pathiterators.kpath.KPathPathIterator;
import com.charrey.pruning.PartialMatching;
import com.charrey.settings.Settings;
import com.charrey.settings.iterator.ControlPointIteratorStrategy;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The type Path iterator.
 */
public abstract class PathIterator {


    private final GlobalOccupation globalOccupation;


    private final int head;
    private final int tail;
    /**
     * The Refuse longer paths.
     */
    protected final boolean refuseLongerPaths;
    protected long timeoutTime;
    private int counter = 0;
    protected final PartialMatchingProvider partialMatchingProvider;
    protected final OccupationTransaction transaction;


    /**
     * Instantiates a new Path iterator.
     *
     * @param tail the tail
     * @param head the head
     */
    protected PathIterator(int tail,
                           int head,
                           Settings settings,
                           GlobalOccupation globalOccupation,
                           OccupationTransaction transaction,
                           PartialMatchingProvider partialMatchingProvider,
                           long timeoutTime) {
        this.tail = tail;
        this.head = head;
        this.refuseLongerPaths = settings.getRefuseLongerPaths();
        this.partialMatchingProvider = partialMatchingProvider;
        this.globalOccupation = globalOccupation;
        this.transaction = transaction;
        this.timeoutTime = timeoutTime;
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
     * @return the path iterator
     */
    @NotNull
    public static PathIterator get(@NotNull MyGraph targetGraph,
                                   @NotNull UtilityData data,
                                   int tail,
                                   int head,
                                   @NotNull GlobalOccupation occupation,
                                   Supplier<Integer> placementSize,
                                   Settings settings,
                                   PartialMatchingProvider provider,
                                   long timeoutTime,
                                   Set<List<Map<String, Set<String>>>> compatibilityChains) {
        if (targetGraph.getEdge(tail, head) != null) {
            return new SingletonPathIterator(targetGraph, settings, tail, head, provider);
        }
        return switch (settings.getPathIteration().iterationStrategy) {
            case DFS_ARBITRARY, DFS_GREEDY -> {
                if (settings.getDfsCaching()) {
                    yield new CachedDFSPathIterator(targetGraph, settings, tail, head, occupation, placementSize, provider, data.getTargetNeighbours(settings.getPathIteration().iterationStrategy)[head], timeoutTime);
                } else {
                    yield new InPlaceDFSPathIterator(targetGraph, settings, tail, head, occupation, placementSize, provider, settings.getPathIteration(), timeoutTime);
                }
            }
            case CONTROL_POINT -> new ManagedControlPointIterator(targetGraph, settings, tail, head, occupation, placementSize, provider, ((ControlPointIteratorStrategy) settings.getPathIteration()).getMaxControlpoints(), timeoutTime);
            case KPATH -> new KPathPathIterator(targetGraph, settings, tail, head, occupation, placementSize, provider, timeoutTime);
        };
    }

    protected PartialMatching getPartialMatching() {
        PartialMatching fromParent = partialMatchingProvider.getPartialMatching();
        return new PartialMatching(fromParent.getVertexMapping(), fromParent.getEdgeMapping(), new TIntHashSet());
    }


    @Nullable
    public abstract Path getNext();

    @Nullable
    public Path next() {
        int maxPaths = Integer.MAX_VALUE;
        if (counter == maxPaths) {
            return null;
        }
        Path toReturn;
        if (globalOccupation != null) {
            globalOccupation.claimActiveOccupation(transaction);
            toReturn = getNext();
            while (toReturn != null && toReturn.length() == 1) {
                toReturn = getNext();

            }
            globalOccupation.unclaimActiveOccupation();
        } else {
            toReturn = getNext();
            while (toReturn != null && toReturn.length() == 1) {
                toReturn = getNext();

            }
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
