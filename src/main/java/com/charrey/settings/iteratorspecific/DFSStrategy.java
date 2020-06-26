package com.charrey.settings.iteratorspecific;

import com.charrey.settings.PathIterationConstants;

public final class DFSStrategy extends IteratorSettings {

    public DFSStrategy() {
        super(PathIterationConstants.DFS_ARBITRARY);
    }


    @Override
    public int serialized() {
        return PathIterationConstants.DFS_ARBITRARY;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass().equals(o.getClass());
    }

    @Override
    public int hashCode() {
        return PathIterationConstants.DFS_ARBITRARY;
    }
}
