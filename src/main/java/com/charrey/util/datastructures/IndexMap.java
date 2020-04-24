package com.charrey.util.datastructures;

import com.charrey.graph.Vertex;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class IndexMap <T> implements Map<Vertex, T> {

    Vertex[] keys;
    List<T> values;
    private int size = 0;

    public IndexMap(int maxSize) {
        keys = new Vertex[maxSize];
        values = new ArrayList<>(maxSize);
        for (int i = 0; i < maxSize; i++) {
            values.add(null);
        }
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            throw new NullPointerException("IndexMap may not contain null keys.");
        }
        return keys[((Vertex) key).data()] == key;
    }

    @Override
    public boolean containsValue(Object value) {
        return values.stream().anyMatch(value::equals);
    }

    @Override
    public @Nonnull T get(Object key) {
        if (key == null) {
            throw new NullPointerException("IndexMap may not contain null keys.");
        }
        return values.get(((Vertex) key).data());
    }

    @Override
    public T put(Vertex key, T value) {
        if (key == null) {
            throw new NullPointerException("IndexMap may not contain null keys.");
        } else if (value == null) {
            throw new NullPointerException("IndexMap may not contain null values.");
        } else {
            T before = values.get(key.data());
            if (before == null) {
                size++;
            }
            values.set(key.data(), value);
            keys[key.data()] = key;
            return before;
        }
    }

    @Override
    public T remove(Object key) {
        T before = values.get(((Vertex)key).data());
        values.set(((Vertex)key).data(), null);
        keys[((Vertex)key).data()] = null;
        if (before != null) {
            size--;
        }
        return before;
    }

    @Override
    public void putAll(Map<? extends Vertex, ? extends T> m) {
        for (Entry<? extends Vertex, ? extends T> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        keys = new Vertex[keys.length];
        for (int i = 0; i < values.size(); i++) {
            values.set(i, null);
        }
    }

    @Override
    public @Nonnull Set<Vertex> keySet() {
        return Arrays.stream(keys).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @Nonnull Collection<T> values() {
        return values.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @Nonnull Set<Entry<Vertex, T>> entrySet() {
        return keySet().stream().map(x -> new AbstractMap.SimpleEntry<>(x, get(x))).collect(Collectors.toUnmodifiableSet());
    }
}
