package com.charrey.settings.iterator;

import com.charrey.settings.pathiteration.PathIteration;

@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public abstract class IteratorSettings implements Cloneable {


    public final PathIteration iterationStrategy;

    IteratorSettings(PathIteration iterationStrategy) {
        this.iterationStrategy = iterationStrategy;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException();
        }
    }

}
