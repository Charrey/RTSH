package com.charrey.util.datastructures;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * The type Multiple key map.
 *
 * @param <V> the type parameter
 */
public class MultipleKeyMap<V> {

    @NotNull
    private final Map<Integer, Map<Integer, V>> nestedMap2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipleKeyMap<?> that = (MultipleKeyMap<?>) o;
        return nestedMap2.equals(that.nestedMap2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nestedMap2);
    }

    public Set<Entry> entrySet() {
        Set<Entry> res = new HashSet<>();
        nestedMap2.forEach((key, map) -> map.forEach((key2, value) -> res.add(new Entry(key, key2, value))));
        return res;
    }


    /**
     * Instantiates a new Multiple key map.
     */
    public MultipleKeyMap() {
        nestedMap2 = new HashMap<>();
    }

    /**
     * Contains key boolean.
     *
     * @param a the a
     * @param b the b
     * @return the boolean
     */
    public boolean containsKey(Integer a, Integer b) {
        return nestedMap2.containsKey(a) && nestedMap2.get(a).containsKey(b);
    }

    /**
     * Put.
     *
     * @param a            the a
     * @param b            the b
     * @param pathIterator the path iterator
     */
    public void put(Integer a, Integer b, V pathIterator) {
        nestedMap2.putIfAbsent(a, new HashMap<>());
        nestedMap2.get(a).put(b, pathIterator);
    }

    /**
     * Get v.
     *
     * @param a the a
     * @param b the b
     * @return the v
     */
    public V get(int a, int b) {
        return nestedMap2.containsKey(a) ? nestedMap2.get(a).get(b) : null;
    }

    /**
     * Remove.
     *
     * @param a the a
     * @param b the b
     */
    public void remove(int a, int b) {
        if (nestedMap2.containsKey(a)) {
            nestedMap2.get(a).remove(b);
        }
    }

    public class Entry {
        private final V value;
        private final Integer secondKey;
        private final Integer firstKey;

        public Entry(Integer a, Integer b, V value) {
            this.firstKey = a;
            this.secondKey = b;
            this.value = value;
        }

        public Integer getFirstKey() {
            return firstKey;
        }

        public Integer getSecondKey() {
            return secondKey;
        }

        public V getValue() {
            return value;
        }
    }
}
