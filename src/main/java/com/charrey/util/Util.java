package com.charrey.util;

import org.apache.commons.math3.random.RandomGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility methods
 */
public class Util {

    /**
     * Selects a random element from a collection that fulfills a specific predicate.
     *
     * @param <V>        the type of elements in the collection
     * @param collection the collection of elements
     * @param eligable   predicate that determines whether an element is eligable to be returned.
     * @param random     randomgenerator for deterministic randomness
     * @return a random element from the collection
     */
    public static <V> V selectRandom(@NotNull Collection<V> collection, Predicate<V> eligable, RandomGenerator random) {
        List<V> myList = collection.stream().filter(eligable).collect(Collectors.toList());
        return myList.get(random.nextInt(myList.size()));
    }
}
