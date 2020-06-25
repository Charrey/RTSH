package com.charrey.util.datastructures;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Multiple key map.
 *
 * @param <V> the type parameter
 */
public class MultipleKeyMap<V> {

    @NotNull
    private final Map<Integer, Map<Integer, V>> nestedMap2;

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
    public boolean containsKey(int a, int b) {
        return nestedMap2.containsKey(a) && nestedMap2.get(a).containsKey(b);
    }

    /**
     * Put.
     *
     * @param a            the a
     * @param b            the b
     * @param pathIterator the path iterator
     */
    public void put(int a, int b, V pathIterator) {
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

}
