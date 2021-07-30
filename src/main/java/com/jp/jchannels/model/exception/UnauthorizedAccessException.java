package com.jp.jchannels.model.exception;

public class UnauthorizedAccessException extends Exception {

    public UnauthorizedAccessException(String reason) {
        super(reason);
    }
}
