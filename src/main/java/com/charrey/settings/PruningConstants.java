package com.charrey.settings;

/**
 * The class containing integer constants denoting the pruning method to be used in a homeomorphism search.
 */
public class PruningConstants {

    /**
     * Do not perform pruning (fast but least strict)
     */
    public static final int NONE = 0;
    /**
     * Prune when some source graph vertex cannot be placed anymore (slower and stricter)
     */
    public static final int EMPTY_DOMAIN = 1;
    /**
     * Prune when an AllDifferent constraint fails (slowest and strictest)
     */
    public static final int ALL_DIFFERENT = 2;
}
