package com.charrey.router;

import com.charrey.graph.Vertex;

import java.util.Set;
import java.util.TreeSet;

public class LockTable {

    private Set<Vertex> locked = new TreeSet<>();

    private boolean lock(Vertex toLock) {
        return locked.add(toLock);
    }

    private boolean unlock(Vertex toUnlock) {
        return locked.remove(toUnlock);
    }

    private boolean isLocked(Vertex isLocked) {
        return locked.contains(isLocked);
    }

    @Override
    public String toString() {
        return locked.toString();
    }
}
