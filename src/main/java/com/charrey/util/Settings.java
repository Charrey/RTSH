package com.charrey.util;

public class Settings {

    public static final boolean initialLocalizedAllDifferent = true;
    public static final boolean initialGlobalAllDifferent = true;
    public static final boolean refuseLongerPaths = true;
    public static final RunTimeCheck runTimeCheck = RunTimeCheck.NONE;
    public static final DFSStrategy DFSSetting = DFSStrategy.GREEDY;

    public enum  DFSStrategy {
        ARBITRARY, GREEDY
    }

    public enum RunTimeCheck {
        EMPTY_DOMAIN, ALLDIFFERENT, NONE
    }
}
