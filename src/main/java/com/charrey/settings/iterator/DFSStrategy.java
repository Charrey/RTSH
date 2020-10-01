package com.charrey.settings.iterator;

import com.charrey.settings.pathiteration.PathIteration;

public final class DFSStrategy extends IteratorSettings {

    public DFSStrategy() {
        super(PathIteration.DFS_ARBITRARY);
    }


    @Override
    public String toString() {
        return "DFS            ";
    }


    @Override
    public IteratorSettings newInstance() {
        return new DFSStrategy();
    }


}
