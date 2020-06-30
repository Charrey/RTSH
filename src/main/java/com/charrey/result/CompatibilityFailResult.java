package com.charrey.result;

/**
 * Result provided if during the initial domain filtering we could already conclude there does not exist a subgraph
 * homeomorphism
 */
public class CompatibilityFailResult extends HomeomorphismResult {


    public CompatibilityFailResult() {
        super(false, 0L);
    }
}
