package com.charrey.settings.iteratorspecific;

import com.charrey.settings.PathIterationConstants;

public final class KPathStrategy extends IteratorSettings {
    public KPathStrategy() {
        super(PathIterationConstants.KPATH);
    }

    @Override
    public int serialized() {
        return PathIterationConstants.KPATH;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass().equals(o.getClass());
    }

    @Override
    public int hashCode() {
        return PathIterationConstants.KPATH;
    }

    @Override
    public String toString() {
        return "K-Path         ";
    }
}
