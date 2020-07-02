package com.charrey.graph;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;

public class MyEdge implements Comparable<MyEdge>, Serializable {

    public Integer source;
    public Integer target;

    public MyEdge() {
    }

    public MyEdge(Integer source, Integer target) {
        this.source = source;
        this.target = target;
    }

    public void setTarget(Integer targetVertex) {
        target = targetVertex;
    }

    public void setSource(Integer sourceVertex) {
        source = sourceVertex;
    }

    @Override
    public String toString() {
        return "(" + source + ", " + target + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyEdge myEdge = (MyEdge) o;
        return source.equals(myEdge.source) &&
                target.equals(myEdge.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    public int compareTo(@NotNull MyEdge other) {
        return Comparator.comparingInt(x -> ((MyEdge) x).source).thenComparingInt(x -> ((MyEdge) x).target).compare(this, other);
    }

    public static class MyEdgeSupplier implements Supplier<MyEdge>, Serializable {

        @Override
        public MyEdge get() {
            return new MyEdge();
        }
    }
}
