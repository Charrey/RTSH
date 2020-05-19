package com.charrey.util.datastructures;

import com.charrey.graph.Vertex;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

public class MultipleKeyMap<V> {

    private final V[][] nestedMap;

    @SuppressWarnings("unchecked")
    public MultipleKeyMap(int firstDomainSize, int secondDomainSize, Class<?> clazz) {
        nestedMap = (V[][] ) Array.newInstance(clazz, firstDomainSize, secondDomainSize);
    }

    public boolean containsKey(Vertex a, Vertex b) {
        return nestedMap[a.data()][b.data()] != null;
    }

    public void put(Vertex a, Vertex b, V pathIterator) {
        nestedMap[a.data()][b.data()] = pathIterator;
    }

    public V get(Vertex a, Vertex b) {
        return nestedMap[a.data()][b.data()];
    }

    public void remove(Vertex key, Vertex key1) {
        nestedMap[key.data()][key1.data()] = null;
    }

    public Set<V> values() {
        Set<V> res = new HashSet<>();
        for (V[] vs : nestedMap) {
            for (V v : vs) {
                if (v != null) {
                    res.add(v);
                }
            }
        }
        return res;
    }
}
