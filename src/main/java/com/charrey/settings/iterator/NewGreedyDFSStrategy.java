package com.charrey.settings.iterator;

import com.charrey.settings.pathiteration.PathIteration;

public final class NewGreedyDFSStrategy extends IteratorSettings {

    public NewGreedyDFSStrategy() {
        super(PathIteration.DFS_GREEDY);
    }


    @Override
    public String toString() {
        return "New Greedy DFS ";
    }

    @Override
    public Object clone() {
        return super.clone();
    }
}
