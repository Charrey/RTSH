package com.charrey.graph;

import java.util.*;

public class VertexDatabase {

    private final Map<String, Set<String>>[] attributes;

    @SuppressWarnings("unchecked")
    public VertexDatabase(int vertexCount) {
        attributes = new Map[vertexCount];
        for (int i = 0; i < attributes.length; i++) {
            attributes[i] = new HashMap<>();
        }
    }


    public void addAttribute(int vertex, String key, String value) {
        attributes[vertex].putIfAbsent(key, new HashSet<>());
        attributes[vertex].get(key).add(value);
    }

    public Set<String> getAttribute(int vertex, String key) {
        return attributes[vertex].get(key);
    }

    public Set<String> getLabels(int vertex) {
        return attributes[vertex].getOrDefault("label", Collections.emptySet());
    }



}
