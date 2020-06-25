package com.charrey.util.datastructures;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MultipleKeyMap<V> {

    @NotNull
    private final Map<Integer, Map<Integer, V>> nestedMap2;

    public MultipleKeyMap() {
        nestedMap2 = new HashMap<>();
    }

    public boolean containsKey(int a, int b) {
        return nestedMap2.containsKey(a) && nestedMap2.get(a).containsKey(b);
    }

    public void put(int a, int b, V pathIterator) {
        nestedMap2.putIfAbsent(a, new HashMap<>());
        nestedMap2.get(a).put(b, pathIterator);
    }

    public V get(int a, int b) {
        return nestedMap2.containsKey(a) ? nestedMap2.get(a).get(b) : null;
    }

    public void remove(int a, int b) {
        if (nestedMap2.containsKey(a)) {
            nestedMap2.get(a).remove(b);
        }
    }

}
