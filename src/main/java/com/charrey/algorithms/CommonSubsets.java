package com.charrey.algorithms;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommonSubsets {

    public static <V> void retainLeastSet(Set<Set<V>> set) {
        Set<Set<V>> markedForDeletion = new HashSet<>();
        List<Set<V>> list = set.stream().sorted(Comparator.comparingInt(Set::size)).collect(Collectors.toList());
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (markedForDeletion.contains(list.get(j))) {
                    continue;
                }
                if (list.get(i).containsAll(list.get(j))) {
                    markedForDeletion.add(list.get(j));
                }
            }
        }
        set.removeAll(markedForDeletion);
    }

}
