package com.charrey.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultipleKeyMap<K1, K2, V> {

    private Map<K1, Map<K2, V>> nestedMap = new HashMap<>();
    public boolean containsKey(K1 a, K2 b) {
        return nestedMap.containsKey(a) && nestedMap.get(a).containsKey(b);
    }

    public void put(K1 a, K2 b, V pathIterator) {
        nestedMap.putIfAbsent(a, new HashMap<>());
        nestedMap.get(a).put(b, pathIterator);
    }

    public V get(K1 a, K2 b) {
        return nestedMap.get(a).get(b);
    }

    public void remove(K1 key, K2 key1) {
        nestedMap.get(key).remove(key1);
        if (nestedMap.get(key).isEmpty()) {
            nestedMap.remove(key);
        }
    }

    public Set<MultipleKeyMapEntry<K1, K2, V>> entrySet() {
        Set<MultipleKeyMapEntry<K1, K2, V>> res = new HashSet<>();
        for (Map.Entry<K1, Map<K2, V>> outerEntry : nestedMap.entrySet()) {
            for (Map.Entry<K2, V> innerEntry : outerEntry.getValue().entrySet()) {
                res.add(new MultipleKeyMapEntry<>(outerEntry.getKey(), innerEntry.getKey(), innerEntry.getValue()));
            }
        }
        return res;
    }

    public static class MultipleKeyMapEntry<K1, K2, V> {
        private final K2 k2;
        private final K1 k1;

        public MultipleKeyMapEntry(K1 key1, K2 key2, V value) {
            this.k1 = key1;
            this.k2 = key2;
        }

        public K2 getSecondKey() {
            return k2;
        }

        public K1 getFirstKey() {
            return k1;
        }
    }
}
