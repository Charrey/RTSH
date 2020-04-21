package com.charrey.graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Path {

    private final ArrayList<Vertex> path;
    private final BitSet containing;
    //private Set<Vertex> containing;

    public Path(Vertex init, int maxSize) {
        path = new ArrayList<>(maxSize);
        containing = new BitSet(maxSize);
        append(init);
    }

    public Path(Path found) {
        this.path = new ArrayList<>(found.path);
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
        return path.get(path.size()-1);
    }


    public int length() {
        return path.size();
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public void removeHead() {
        Vertex removed = path.remove(path.size()-1);
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
        return path.get(0);
    }

    @Override
    public String toString() {
        return "Path{" + path + '}';
    }
}
