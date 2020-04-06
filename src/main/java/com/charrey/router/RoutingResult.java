package com.charrey.router;

import com.charrey.graph.AttributedVertex;
import com.charrey.graph.Path;

import java.util.Collections;
import java.util.List;

public class RoutingResult {

    private final boolean failed;
    private final List<Path<AttributedVertex>> paths;
    private final LockTable locks;

    private RoutingResult() {
        throw new UnsupportedOperationException();
    }

    public RoutingResult(boolean failed, List<Path<AttributedVertex>> paths, LockTable locks) {
        this.failed = failed;
        this.paths = paths;
        this.locks = locks;
    }

    public static RoutingResult failed() {
        return new RoutingResult(true, Collections.emptyList(), new LockTable());
    }

    public boolean hasFailed() {
        return failed;
    }

    public boolean requiresExtraVertices() {
        return !paths.isEmpty();
    }

    public LockTable getLocks() {
        return locks;
    }
}
