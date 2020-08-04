package com.charrey.settings.iterator;

import com.charrey.settings.pathiteration.PathIteration;

public final class OldGreedyDFSStrategy extends IteratorSettings {

    public OldGreedyDFSStrategy() {
        super(PathIteration.DFS_GREEDY);
    }


    @Override
    public String toString() {
        return "Old Greedy DFS ";
    }

    @Override
    public Object clone() {
        return new OldGreedyDFSStrategy();
    }
}
