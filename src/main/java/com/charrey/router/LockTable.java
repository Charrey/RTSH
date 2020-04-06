package com.charrey.router;

import java.util.Set;
import java.util.TreeSet;

public class LockTable<V extends Comparable<V>> {

    private Set<V> locked = new TreeSet<>();

    private boolean lock(V toLock) {
        return locked.add(toLock);
    }

    private boolean unlock(V toUnlock) {
        return locked.remove(toUnlock);
    }

    private boolean isLocked(V isLocked) {
        return locked.contains(isLocked);
    }

    @Override
    public String toString() {
        return locked.toString();
    }
}
