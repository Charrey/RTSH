package com.charrey.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Path {

    private LinkedList<Vertex> path;
    private Set<Vertex> containing;

    public Path(Vertex init) {
        path = new LinkedList<>();
        containing = new HashSet<>();
        append(init);
    }

    public Path(Path found) {
        this.path = new LinkedList<>(found.path);
        this.containing = new HashSet<>(found.containing);
    }

    public boolean append(Vertex toAdd) {
        if (containing.contains(toAdd)) {
            return false;
        }
        containing.add(toAdd);
        path.add(toAdd);
        return true;
    }

    public Vertex head() {
        return path.getLast();
    }


    public int length() {
        return containing.size();
    }

    public boolean isEmpty() {
        return containing.isEmpty();
    }

    public void removeHead() {
        Vertex removed = path.removeLast();
        boolean wasRemoved = containing.remove(removed);
        assert wasRemoved;
    }

    public boolean contains(Vertex vertex) {
        return containing.contains(vertex);
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
