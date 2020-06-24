package com.charrey.util.datastructures;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public class MultipleKeyMap<V> {

    @NotNull
    private final V[][] nestedMap;

    @SuppressWarnings("unchecked")
    public MultipleKeyMap(int firstDomainSize, int secondDomainSize, Class<?> clazz) {
        nestedMap = (V[][] ) Array.newInstance(clazz, firstDomainSize, secondDomainSize);
    }

    public boolean containsKey(int a, int b) {
        return nestedMap[a][b] != null;
    }

    public void put(int a, int b, V pathIterator) {
        nestedMap[a][b] = pathIterator;
    }

    public V get(int a, int b) {
        return nestedMap[a][b];
    }

    public void remove(int key, int key1) {
        nestedMap[key][key1] = null;
    }

}
