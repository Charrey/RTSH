package com.charrey.matching.matchResults.edge;

import com.charrey.graph.Path;

import java.util.Objects;

public class SuccessPathResult extends EdgeMatchResult {
    private final Path path;

    public SuccessPathResult(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("path=").append(path);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuccessPathResult that = (SuccessPathResult) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
