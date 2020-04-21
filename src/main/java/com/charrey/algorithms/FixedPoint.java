package com.charrey.algorithms;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class FixedPoint {

    public static  <V> Set<V> explore(V start, Function<V, Set<V>> exploration) {
        Deque<V> toExplore = new ArrayDeque<>();
        Set<V> explored = new HashSet<>();
        toExplore.add(start);
        while (!toExplore.isEmpty()) {
            V exploring = toExplore.poll();
            exploration.apply(exploring).forEach(v -> {
                if (!explored.contains(v) && !toExplore.contains(v) && !exploring.equals(v)) {
                    toExplore.add(v);
                }
            });
            explored.add(exploring);
        }
        return explored;
    }
}
