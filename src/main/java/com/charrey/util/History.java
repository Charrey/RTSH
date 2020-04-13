package com.charrey.util;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.router.PathIterator;
import org.jgrapht.alg.util.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;

public class History {

    private Deque<Object> deque = new ArrayDeque<>();

    public boolean isEmpty() {
        return deque.isEmpty();
    }

    public Object peekFirst() {
        return deque.peekFirst();
    }

    public Object removeFirst() {
        return deque.removeFirst();
    }

    public void addFirst(Pair<Integer, Vertex> nextCandidate) {
        deque.addFirst(nextCandidate);
    }

    public void addFirst(Path nextpath) {
        deque.addFirst(nextpath);
    }

    @Override
    public String toString() {
        return deque.toString();
    }

    public void onRemove(Object whenRemoved, Runnable o) {

    }
}
