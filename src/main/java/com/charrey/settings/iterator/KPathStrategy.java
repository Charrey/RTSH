package com.charrey.settings.iterator;

import com.charrey.settings.pathiteration.PathIteration;

public final class KPathStrategy extends IteratorSettings {
    public KPathStrategy() {
        super(PathIteration.KPATH);
    }

    @Override
    public String toString() {
        return "K-Path         ";
    }

    @Override
    public IteratorSettings newInstance() {
        return new KPathStrategy();
    }
}
