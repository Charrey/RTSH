package com.charrey.settings.pruning.domainfilter;

public class NReachabilityFiltering implements FilteringSettings {

    private final int level;

    public NReachabilityFiltering(int level) {
        this.level = level;
    }

    @Override
    public FilteringSettings newInstance() {
        return new NReachabilityFiltering(level);
    }

    public int getLevel() {
        return level;
    }
}
