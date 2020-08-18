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
    private final int maxPaths;
    private final Supplier<Integer> placementSize;
    protected long timeoutTime;
    private int counter = 0;
    protected final PartialMatchingProvider partialMatchingProvider;
    protected final OccupationTransaction transaction;


    /**
     * Instantiates a new Path iterator.
     *  @param tail the tail
     * @param head the head
     * @param placementSize
     */
    protected PathIterator(int tail,
                           int head,
                           Settings settings,
                           GlobalOccupation globalOccupation,
                           OccupationTransaction transaction,
                           PartialMatchingProvider partialMatchingProvider,
                           long timeoutTime,
                           Supplier<Integer> placementSize) {
        this.tail = tail;
        this.head = head;
        this.refuseLongerPaths = settings.getRefuseLongerPaths();
        this.partialMatchingProvider = partialMatchingProvider;
        this.globalOccupation = globalOccupation;
        this.transaction = transaction;
        this.timeoutTime = timeoutTime;
        this.maxPaths = settings.getPathsLimit();
        this.placementSize = placementSize;
    }


    protected PartialMatching getPartialMatching() {
        PartialMatching fromParent = partialMatchingProvider.getPartialMatching();
        return new PartialMatching(fromParent.getVertexMapping(), fromParent.getEdgeMapping(), new TIntHashSet());
    }


    @Nullable
    public abstract Path getNext();

    @Nullable
    public Path next() {
        if (counter == maxPaths) {
            uncommit();
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

    protected final void uncommit() {
        transaction.uncommit(placementSize.get());
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
