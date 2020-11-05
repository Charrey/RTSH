package com.charrey.settings.iterator;

import com.charrey.settings.pathiteration.PathIteration;

public abstract class IteratorSettings implements Cloneable {


    public final PathIteration iterationStrategy;

    IteratorSettings(PathIteration iterationStrategy) {
        this.iterationStrategy = iterationStrategy;
    }

    public abstract IteratorSettings newInstance();
}
