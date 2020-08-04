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
    public Object clone() {
        return new KPathStrategy();
    }
}
