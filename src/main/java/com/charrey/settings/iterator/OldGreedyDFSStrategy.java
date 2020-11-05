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
        Object o = super.clone();
        return new OldGreedyDFSStrategy();
    }

    @Override
    public IteratorSettings newInstance() {
        return new OldGreedyDFSStrategy();
    }
}
