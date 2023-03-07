package com.adtsw.jchannels.model;

import com.adtsw.jchannels.model.auth.SessionInfo;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class HttpRequest {

    private final String domain;
    private final String uri;
    private final Map<String, String> headers;
    private final Map<String, String> cookies;
    private final String body;
    private final SessionInfo sessionInfo;
    
    private HttpRequest(String domain, String uri, String body, Map<String, String> headers, 
                       Map<String, String> cookies, SessionInfo sessionInfo) {
    
        this.domain = domain;
        this.uri = uri;
        this.headers = headers;
        this.cookies = cookies;
        this.body = body;
        this.sessionInfo = sessionInfo;
    }

    public static HttpRequestBuilder getBuilder() {
        return new HttpRequestBuilder();
    }

    public static HttpRequestBuilder getBuilder(HttpRequest request) {
        return new HttpRequestBuilder(request);
    }

    public static class HttpRequestBuilder {

        private String domain = null;
        private String uri = null;
        private Map<String, String> headers = null;
        private Map<String, String> cookies = null;
        private String body = null;
        private SessionInfo sessionInfo = null;
    
        public HttpRequestBuilder() {
        }
        
        public HttpRequestBuilder(HttpRequest request) {
            this.domain = request.getDomain();
            this.uri = request.getUri();
            this.body = request.getBody();
            this.headers = request.getHeaders();
            this.cookies = request.getCookies();
            this.sessionInfo = request.getSessionInfo();
        }
    
        public HttpRequestBuilder withDomain(String domain) {
            this.domain = domain;
            return this;
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
            return new HttpRequest(
                domain, uri, body, 
                headers == null ? new HashMap<>() : headers, 
                cookies == null ? new HashMap<>() : cookies, 
                sessionInfo
            );
        }
    }
    
}
