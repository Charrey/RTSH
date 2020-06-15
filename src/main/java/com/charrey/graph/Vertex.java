package com.charrey.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;

public class Vertex implements Serializable, Comparable<Vertex> {

    private static int counter = 0;
    private final int counterValue;
    private int data;
    private final Map<String, Set<Attribute>> attributes = new HashMap<>();

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public Vertex(int data) {
        this.data = data;
        counterValue = ++counter;
    }

    public void addAttribute(String key, String value) {
        attributes.putIfAbsent(key, new HashSet<>());
        attributes.get(key).add(new DefaultAttribute<>(value, AttributeType.STRING));
    }


    @NotNull
    @Override
    public String toString() {
        return "[" + data + "]";
    }

    public Set<Attribute> getLabels() {
        return attributes.getOrDefault("label", Collections.emptySet());
    }

    public void setData(int newData) {
        data = newData;
    }

    @Override
    public int compareTo(@Nonnull Vertex o) {
        return Integer.compare(data, o.data);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex that = (Vertex) o;
        return counterValue == that.counterValue;
    }

    @Override
    public int hashCode() {
        return counterValue;
    }

    public int data() {
        return data;
    }
}
