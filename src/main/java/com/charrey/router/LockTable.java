package com.charrey.router;

import com.charrey.graph.AttributedVertex;

import java.util.Set;
import java.util.TreeSet;

public class LockTable {

    private Set<AttributedVertex> locked = new TreeSet<>();

    private boolean lock(AttributedVertex toLock) {
        return locked.add(toLock);
    }

    private boolean unlock(AttributedVertex toUnlock) {
        return locked.remove(toUnlock);
    }

    private boolean isLocked(AttributedVertex isLocked) {
        return locked.contains(isLocked);
    }

    @Override
    public String toString() {
        return locked.toString();
    }
}
