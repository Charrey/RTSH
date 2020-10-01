package com.charrey.pruning;

import java.util.function.Supplier;

/**
 * Exception thrown when pruning should be applied instead of matching
 */
public class DomainCheckerException extends Exception {
    private transient final Supplier<String> messageProvider;

    /**
     * Instantiates a new DomainCheckerException
     *
     * @param s message to display if printed to the console
     */
    public DomainCheckerException(Supplier<String> s) {
        super();
        this.messageProvider = s;
    }

    @Override
    public String getMessage() {
        return messageProvider.get();
    }
}
