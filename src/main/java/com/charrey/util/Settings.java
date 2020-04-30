package com.charrey.util;

public class Settings {

    public static final boolean initialLocalizedAllDifferent = true;
    public static final boolean initialGlobalAllDifferent = true;
    public static final boolean refuseLongerPaths = true;
    public static final boolean checkForEmptyDomain = true;
    public static final DFSStrategy DFSSetting = DFSStrategy.GREEDY;

    public enum  DFSStrategy {
        ARBITRARY, GREEDY
    }
}
