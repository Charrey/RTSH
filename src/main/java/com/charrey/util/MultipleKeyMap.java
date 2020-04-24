package com.charrey.util;

import com.charrey.graph.Vertex;

import java.util.*;

public class MultipleKeyMap<V> {

    private final int secondDomainSize;

    public MultipleKeyMap(int firstDomainSize, int secondDomainSize) {
        nestedMap = new IndexMap<>(firstDomainSize);
        this.secondDomainSize = secondDomainSize;
    }

    private final Map<Vertex, Map<Vertex, V>> nestedMap;
    public boolean containsKey(Vertex a, Vertex b) {
        return nestedMap.containsKey(a) && nestedMap.get(a).containsKey(b);
    }

    public void put(Vertex a, Vertex b, V pathIterator) {
        nestedMap.putIfAbsent(a, new IndexMap<>(secondDomainSize));
        nestedMap.get(a).put(b, pathIterator);
    }

    public V get(Vertex a, Vertex b) {
        return nestedMap.get(a).get(b);
    }

    public void remove(Vertex key, Vertex key1) {
        nestedMap.get(key).remove(key1);
    }

    public void removeIfPresent(Vertex key, Vertex key2) {
        if (containsKey(key, key2)) {
            remove(key, key2);
        }
    }
}
