package com.charrey.settings.iterator;

import com.charrey.settings.PathIterationConstants;

public final class GreedyDFSStrategy extends IteratorSettings {

    public GreedyDFSStrategy() {
        super(PathIterationConstants.DFS_GREEDY);
    }

    @Override
    public int serialized() {
        return PathIterationConstants.DFS_GREEDY.ordinal();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass().equals(o.getClass());
    }

    @Override
    public int hashCode() {
        return PathIterationConstants.DFS_GREEDY.ordinal();
    }

    @Override
    public String toString() {
        return "Greedy DFS     ";
    }
}
