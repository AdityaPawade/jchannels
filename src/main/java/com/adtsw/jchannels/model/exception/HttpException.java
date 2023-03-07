package com.adtsw.jchannels.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpException extends Exception {

    private final int statusCode;
    private final String reason;
    private Throwable cause;

    public HttpException(int statusCode, String reason) {
        super("Http StatusCode (" + statusCode + "), Message : " + reason);
        this.statusCode = statusCode;
        this.reason = reason;
    }
}
