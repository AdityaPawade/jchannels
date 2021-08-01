package com.adtsw.jchannels.model;

import com.adtsw.jchannels.model.auth.SessionInfo;

import java.util.Map;

public class HttpRequestBuilder {

    private String uri = null;
    private Map<String, String> headers = null;
    private Map<String, String> cookies = null;
    private String body = null;
    private SessionInfo sessionInfo = null;

    public HttpRequestBuilder() {
    }
    
    public HttpRequestBuilder(HttpRequest request) {
        this.uri = request.getUri();
        this.body = request.getBody();
        this.headers = request.getHeaders();
        this.cookies = request.getCookies();
        this.sessionInfo = request.getSessionInfo();
    }

    public HttpRequestBuilder withUri(String uri) {
        this.uri = uri;
        return this;
    }

    public HttpRequestBuilder withBody(String body) {
        this.body = body;
        return this;
    }
    
    public HttpRequestBuilder withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequestBuilder withCookies(Map<String, String> cookies) {
        this.cookies = cookies;
        return this;
    }

    public HttpRequestBuilder withSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
        return this;
    }
    
    public HttpRequest build() {
        return new HttpRequest(uri, body, headers, cookies, sessionInfo);
    }
}
