package com.charrey.graph;

import java.util.*;
import java.util.stream.Stream;

public class Path {

    private LinkedList<Vertex> path;
    private BitSet containing;
    //private Set<Vertex> containing;

    public Path(Vertex init, int maxSize) {
        path = new LinkedList<>();
        containing = new BitSet(maxSize);
        append(init);
    }

    public Path(Path found) {
        this.path = new LinkedList<>(found.path);
        this.containing = (BitSet) found.containing.clone();
    }

    public boolean append(Vertex toAdd) {
        if (containing.get(toAdd.intData())) {
            return false;
        }
        containing.set(toAdd.intData());
        path.add(toAdd);
        return true;
    }

    public Vertex head() {
        return path.getLast();
    }


    public int length() {
        return path.size();
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public void removeHead() {
        Vertex removed = path.removeLast();
        containing.clear(removed.intData());
    }

    public boolean contains(Vertex vertex) {
        return path.contains(vertex);
    }

    public Vertex get(int i) {
        return path.get(i);
    }

    public List<Vertex> getPath() {
        return path;
    }

    public List<Vertex> intermediate() {
        return path.subList(1, path.size() - 1);
    }

    public Vertex tail() {
        return path.getFirst();
    }

    @Override
    public String toString() {
        return "Path{" + path + '}';
    }
}
