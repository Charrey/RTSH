package com.charrey.result;

public final class FailResult extends HomeomorphismResult {
    public FailResult(long iterations, double mem) {
        super(false, iterations, mem);
    }
}
