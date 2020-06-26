package com.charrey.pathiterators.controlpoint;

/**
 * Class that contains settings for the pathiteration strategy ContolPoint
 */
public class ControlPointIteratorRelevantSettings {

    /**
     * The default value of the value of the log setting.
     */
    private static final boolean defaultLog = false;
    /**
     * Whether the iteration process should be printed in detail.
     */
    public boolean log;
    /**
     * Whether to refuse paths that take unnecessarily many resources.
     */
    public boolean refuseLongerPaths;


    /**
     * Instantiates a new instance of this class.
     *
     * @param refuseLongerPaths Whether to refuse paths that take unnecessarily many resources.
     */
    ControlPointIteratorRelevantSettings(boolean refuseLongerPaths) {
        this.log = defaultLog;
        this.refuseLongerPaths = refuseLongerPaths;
    }

}
