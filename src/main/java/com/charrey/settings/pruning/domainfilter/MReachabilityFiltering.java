package com.charrey.settings.pruning.domainfilter;

public class MReachabilityFiltering implements FilteringSettings {


    private boolean cached = false;

    public void setCached() {
        cached = true;
    }

    @Override
    public FilteringSettings newInstance() {
        MReachabilityFiltering toReturn =  new MReachabilityFiltering();
        if (cached) {
            toReturn.setCached();
        }
        return toReturn;
    }


}
