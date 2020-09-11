package com.charrey.settings.pruning.domainfilter;

public class NoFiltering implements FilteringSettings {



    @Override
    public FilteringSettings newInstance() {
        return this;
    }


}
