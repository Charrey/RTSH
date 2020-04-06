package com.charrey.router;

import com.charrey.graph.Path;

import java.util.Collections;
import java.util.List;

public class RoutingResult<V extends Comparable<V>> {

    private final boolean failed;
    private final List<Path<V>> paths;
    private final LockTable<V> locks;

    private RoutingResult() {
        failed = true;
        paths = null;
        locks = null;
    }

    public RoutingResult(boolean failed, List<Path<V>> paths, LockTable<V> locks) {
        this.failed = failed;
        this.paths = paths;
        this.locks = locks;
    }

    public static RoutingResult failed() {
        return new RoutingResult(true, Collections.emptyList(), new LockTable<>());
    }

    public boolean hasFailed() {
        return failed;
    }

    public boolean requiresExtraVertices() {
        return !paths.isEmpty();
    }

    public LockTable<V> getLocks() {
        return locks;
    }
}
