package com.charrey.util.datastructures;

import com.charrey.graph.Vertex;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public class MultipleKeyMap<V> {

    @NotNull
    private final V[][] nestedMap;

    @SuppressWarnings("unchecked")
    public MultipleKeyMap(int firstDomainSize, int secondDomainSize, Class<?> clazz) {
        nestedMap = (V[][] ) Array.newInstance(clazz, firstDomainSize, secondDomainSize);
    }

    public boolean containsKey(@NotNull Vertex a, @NotNull Vertex b) {
        return nestedMap[a.data()][b.data()] != null;
    }

    public void put(@NotNull Vertex a, @NotNull Vertex b, V pathIterator) {
        nestedMap[a.data()][b.data()] = pathIterator;
    }

    public V get(@NotNull Vertex a, @NotNull Vertex b) {
        return nestedMap[a.data()][b.data()];
    }

    public void remove(@NotNull Vertex key, @NotNull Vertex key1) {
        nestedMap[key.data()][key1.data()] = null;
    }

}
