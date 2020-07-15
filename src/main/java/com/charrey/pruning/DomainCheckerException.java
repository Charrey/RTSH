package com.charrey.pruning;

/**
 * Exception thrown when pruning should be applied instead of matching
 */
public class DomainCheckerException extends Exception {
    /**
     * Instantiates a new DomainCheckerException
     *
     * @param s message to display if printed to the console
     */
    public DomainCheckerException(String s) {
        super(s);
    }
}
