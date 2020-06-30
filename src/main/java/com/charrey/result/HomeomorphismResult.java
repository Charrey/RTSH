package com.charrey.result;

/**
 * The result from a subgraph homeomorphism search
 */
public abstract class HomeomorphismResult {

    /**
     * Whether a homeomorphism was found (true = not found, false = found).
     */
    public final boolean succeed;
    public final long iterations;


    protected HomeomorphismResult(boolean succeed, long iterations) {
        this.succeed = succeed;
        this.iterations = iterations;
    }

}
