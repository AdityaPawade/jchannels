package com.adtsw.jchannels.model;

import com.adtsw.jchannels.model.exception.HttpException;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class HttpResponse {

    private final String body;
    private final Map<String, String> headers;
    private final Map<String, String> cookies;
    private final HttpException exception;

    public HttpResponse(String body) {
        this(body, new HashMap<>(), new HashMap<>(), null);
    }

    public HttpResponse(String body, Map<String, String> headers, Map<String, String> cookies) {
        this(body, headers, cookies, null);
    }

    public HttpResponse(HttpException exception) {
        this(null, new HashMap<>(), new HashMap<>(), exception);
    }
    
    public boolean isSuccess() {
        return exception == null;
    }
}
