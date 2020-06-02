package com.charrey.util;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHandler extends Handler {
    @Override
    public void publish(LogRecord record) {
        //System.out.println(record.getMessage());
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}