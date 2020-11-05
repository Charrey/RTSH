package com.charrey.pathiterators;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pruning.serial.PartialMatching;
import com.charrey.settings.Settings;
import gnu.trove.set.TIntSet;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The type Path iterator.
 */
public abstract class PathIterator {


    final GlobalOccupation globalOccupation;


    private final int head;
    private final int tail;
    /**
     * The Refuse longer paths.
     */
    protected final boolean refuseLongerPaths;
    private final int maxPaths;
    final Supplier<Integer> placementSize;
    private final int cripple;
    private final MyGraph graph;
    protected long timeoutTime;
    private int counter = 0;
    protected final PartialMatchingProvider partialMatchingProvider;
    protected OccupationTransaction transaction;


    /**
     * Instantiates a new Path iterator.
     *  @param tail the tail
     * @param head the head
     * @param placementSize
     */
    protected PathIterator(MyGraph graph,
                           int tail,
                           int head,
                           Settings settings,
                           GlobalOccupation globalOccupation,
                           OccupationTransaction transaction,
                           PartialMatchingProvider partialMatchingProvider,
                           long timeoutTime,
                           Supplier<Integer> placementSize,
                           int cripple) {
        this.graph = graph;
        this.tail = tail;
        this.head = head;
        this.refuseLongerPaths = settings.getRefuseLongerPaths();
        this.partialMatchingProvider = partialMatchingProvider;
        this.globalOccupation = globalOccupation;
        this.transaction = transaction;
        this.timeoutTime = timeoutTime;
        this.maxPaths = settings.getPathsLimit();
        this.placementSize = placementSize;
        this.cripple = cripple;
    }


    protected PartialMatching getPartialMatching() {
        PartialMatching fromParent = partialMatchingProvider.getPartialMatching();
        return new PartialMatching(fromParent.getVertexMapping(), fromParent.getEdgeMapping(), getLocallyOccupied());
    }

    public abstract TIntSet getLocallyOccupied();


    @Nullable
    public abstract Path getNext();

    @Nullable
    public Path next() {
        if (counter == maxPaths) {
            uncommit();
            return null;
        }
        Set<MyEdge> removed = new HashSet<>();
        for (int i = 0; i < cripple; i++) {
            removed.add(graph.removeEdge(tail, head));
        }
        Path toReturn = getNext();
        while (toReturn != null && toReturn.length() == 1) {
            toReturn = getNext();
        }
        removed.forEach(x -> graph.addEdge(tail, head, x));
        counter++;
        return toReturn == null ? null : new Path(toReturn);
    }

    protected final void uncommit() {
        if (transaction != null) {
            transaction.uncommit(placementSize.get(), this::getPartialMatching);
        }
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

    private int sourceGraphFrom;
    private int sourceGraphTo;

    public void setSourceGraphFrom(int sourceGraphFrom) {
        this.sourceGraphFrom = sourceGraphFrom;
    }

    public void setSourceGraphTo(int sourceGraphTo) {
        this.sourceGraphTo = sourceGraphTo;
    }

    public int getSourceGraphFrom() {
        return sourceGraphFrom;
    }

    public int getSourceGraphTo() {
        return sourceGraphTo;
    }
}
