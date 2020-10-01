package com.charrey.pathiterators;

import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.settings.Settings;
import gnu.trove.set.TIntSet;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class VoidPathIterator extends PathIterator{
    /**
     * Instantiates a new Path iterator.
     *
     */
    protected VoidPathIterator(Settings settings) {
        super(null, -1, -1, settings, null, null, null, Long.MAX_VALUE, null, 0);
    }

    @Override
    public TIntSet getLocallyOccupied() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Path getNext() {
        return null;
    }

    @Override
    public String debugInfo() {
        return "void!";
    }
}
