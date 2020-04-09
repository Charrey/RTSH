package com.charrey.matching;

import com.charrey.graph.Path;
import com.charrey.matching.matchResults.edge.EdgeMatchResult;

public class SuccessPathResult extends EdgeMatchResult {
    private final Path path;

    public SuccessPathResult(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
