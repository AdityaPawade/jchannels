package com.adtsw.jchannels.model;

public class Constants {

    public static final String GET_TOPIC = "get";

    public static final String POST_TOPIC = "post";
    
    public static final String OPEN_SCOPE = "open";
    
    public static final String AUTHENTICATED_SCOPE = "authenticated";
    
    public static final String COOKIE_HEADER = "cookie";
    
    public static final String SET_COOKIE_HEADER = "Set-Cookie";
    
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    public static final String AUTHORIZATION_BEARER_TYPE = "Bearer";

    public static final String SCOPE_CLAIM = "scope";
    
    public static class WS {
        
        public static final String CLEAR_MESSAGE = "$!o!CLEAR!o!$";
        
        public static final String SHUTDOWN_MESSAGE = "$!o!SHUTDOWN!o!$";
        
        public static final String ERROR_MESSAGE = "$!o!ERROR!o!$";

        public static final String REQUEST_TOPIC = "request";
        
        public static final String RESPONSE_TOPIC = "response";
        
        public static final String OPS_TOPIC = "operation";
    }
}
