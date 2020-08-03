package com.charrey.util.datastructures;

import gnu.trove.iterator.TIntIterator;

import java.util.Iterator;

public class TroveIterator implements Iterator<Integer> {
    private final TIntIterator inner;

    public TroveIterator(TIntIterator iterator) {
        this.inner = iterator;
    }

    @Override
    public boolean hasNext() {
        return inner.hasNext();
    }

    @Override
    public Integer next() {
        return inner.next();
    }
}
