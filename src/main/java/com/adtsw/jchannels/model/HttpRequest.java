package com.adtsw.jchannels.model;

import com.adtsw.jchannels.model.auth.SessionInfo;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class HttpRequest {

    private final String uri;
    private final Map<String, String> headers;
    private final Map<String, String> cookies;
    private final String body;
    private final SessionInfo sessionInfo;
    
    public HttpRequest(String uri) {
        this(uri, null, new HashMap<>(), new HashMap<>());
    }

    public HttpRequest(String uri, Map<String, String> headers) {
        this(uri, null, headers, new HashMap<>());
    }

    public HttpRequest(String uri, Map<String, String> headers, Map<String, String> cookies) {
        this(uri, null, headers, cookies);
    }

    public HttpRequest(String uri, String body) {
        this(uri, body, new HashMap<>(), new HashMap<>());
    }

    public HttpRequest(String uri, String body, Map<String, String> headers) {
        this(uri, body, headers, new HashMap<>());
    }

    public HttpRequest(String uri, String body, Map<String, String> headers, Map<String, String> cookies) {
        this(uri, body, headers, cookies, null);
    }

    public HttpRequest(String uri, String body, Map<String, String> headers, 
                       Map<String, String> cookies, SessionInfo sessionInfo) {
    
        this.uri = uri;
        this.headers = headers;
        this.cookies = cookies;
        this.body = body;
        this.sessionInfo = sessionInfo;
    }
}
