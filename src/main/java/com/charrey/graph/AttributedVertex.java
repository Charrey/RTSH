package com.charrey.graph;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;

import java.util.*;

public class AttributedVertex<C extends Comparable> implements Comparable<AttributedVertex<C>>{

    private static int counter = 0;
    private final int counterValue;
    public C id;
    protected Map<String, Set<Attribute>> attributes = new HashMap<>();

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public AttributedVertex(C a) {
        id = a;
        counterValue = ++counter;
    }

    public AttributedVertex<C> addLabel(String label) {
        attributes.putIfAbsent("label", new HashSet<>());
        attributes.get("label").add(new DefaultAttribute<>(label, AttributeType.STRING));
        return this;
    }

    public Map<String, Attribute> getAttributes() {
        Map<String, Attribute> res = new HashMap<>();
        for (Map.Entry<String, Set<Attribute>> entry : attributes.entrySet()) {
            res.put(entry.getKey(), new DefaultAttribute<>(entry.getValue().toString(), AttributeType.STRING));
        }
        return res;
    }

    @Override
    public String toString() {
        return "[" + id + "]";
    }

    public Set<Attribute> getLabels() {
        return attributes.getOrDefault("label", Collections.emptySet());
    }

    public int getIntId() {
        return counterValue;
    }

    public boolean containsLabel(String routing) {
        return attributes.getOrDefault("label", Collections.emptySet()).contains(new DefaultAttribute<>(routing, AttributeType.STRING));
    }

    public void setId(C newId) {
        id = newId;
    }

    @Override
    public int compareTo(AttributedVertex<C> o) {
        return id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributedVertex<?> that = (AttributedVertex<?>) o;
        return counterValue == that.counterValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(counterValue);
    }
}
