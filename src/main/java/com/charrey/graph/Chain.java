package com.charrey.graph;

import java.util.*;

public class Chain {

    private LinkedList<Set<String>> chain = new LinkedList<>();
    private boolean locked = false;

    @Override
    public String toString() {
        return chain.toString();
    }

    public void prepend(Set<String> labels) {
        assert labels != null;
        if (locked) {
            throw new IllegalStateException();
        }
        chain.add(0, labels);
    }

    public void append(Set<String> labels) {
        assert labels != null;
        if (locked) {
            throw new IllegalStateException();
        }
        chain.add(labels);
    }

    public boolean compatible(Path path) {
        assert !chain.contains(null);
        ListIterator<Set<String>> chainIndex = chain.listIterator();
        for (int vertex : path.intermediate()) {
            if (!chainIndex.hasNext()) {
                return true;
            }
            Collection<String> labels = ((MyGraph)path.getGraph()).getLabels(vertex);
            Set<String> next = chainIndex.next();
            if (!labels.containsAll(next)) {
                chainIndex.previous();
            }
        }
        return !chainIndex.hasNext();
    }


    public Chain lock() {
        locked = true;
        return this;
    }
}
