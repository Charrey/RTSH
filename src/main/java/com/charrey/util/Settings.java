package com.charrey.util;

public class Settings {

    public static final boolean initialLocalizedAllDifferent = true;
    public static final boolean initialGlobalAllDifferent = true;
    public static final boolean refuseLongerPaths = true;
    public static final RunTimeCheck runTimeCheck = RunTimeCheck.NONE;


    public static final PathIterationStrategy pathIteration = PathIterationStrategy.CONTROL_POINT;

    public enum PathIterationStrategy {
        DFS_ARBITRARY, DFS_GREEDY, CONTROL_POINT
    }

    public enum RunTimeCheck {
        EMPTY_DOMAIN, ALL_DIFFERENT, NONE
    }
}
