package com.charrey.util;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Logger handler that prints all messages to the console
 */
public class LogHandler extends Handler {
    @Override
    public void publish(LogRecord record) {
        System.out.println(record.getMessage());
    }

    @Override
    public void flush() {
        System.out.flush();
    }

    @Override
    public void close() throws SecurityException {

    }
}