package com.charrey.util;

import org.apache.commons.math3.random.RandomGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Util {

    public static  <V> V pickRandom(@NotNull Collection<V> collection, @NotNull RandomGenerator random) {
        List<V> list = new LinkedList<>(collection);
        return list.get(random.nextInt(list.size()));
    }



    public static <V> V selectRandom(@NotNull Set<V> set, Predicate<V> eligable, Random random) {
        List<V> myList = set.stream().filter(eligable).collect(Collectors.toList());
        return myList.get(random.nextInt(myList.size()));
    }
}
