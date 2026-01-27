package com.mad.pogoenhancer.utils;

public class RunException extends Exception {
    public RunException() {
        super();
    }

    public RunException(String detailMessage) {
        super(detailMessage);
    }

    public RunException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}