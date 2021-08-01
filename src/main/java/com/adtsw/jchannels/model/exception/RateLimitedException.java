package com.adtsw.jchannels.model.exception;

public class RateLimitedException extends Exception {

    public RateLimitedException(String reason) {
        super(reason);
    }
}
