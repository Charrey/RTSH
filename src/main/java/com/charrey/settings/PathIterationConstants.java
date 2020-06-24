package com.charrey.settings;

/**
 * The class containing integer constants denoting the path iteration strategy to be used in a homeomorphism search.
 */
public final class PathIterationConstants {

    /**
     * Constant to be used for generation of paths using DFS
     */
    public static final int DFS_ARBITRARY = 0;
    /**
     * Constant to be used for generation of paths using greedy DFS
     */
    public static final int DFS_GREEDY = 1;
    /**
     * Constant to be used for generation of paths using control point routing
     */
    public static final int CONTROL_POINT = 2;
    /**
     * Constant to be used for generation of paths using K-Path routing
     */
    public static final int KPATH = 4;
}
