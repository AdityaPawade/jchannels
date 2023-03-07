package com.adtsw.jchannels.client.http;

import com.adtsw.jchannels.messaging.sink.IMessageActor;
import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpClientBuilder {

    private static Logger logger = LogManager.getLogger(HttpClientBuilder.class);
    
    private Integer timeoutInSeconds = null;
    private IMessageActor<HttpRequest, HttpResponse> listener;
    
    public HttpClientBuilder() {
    }
    
    public HttpClientBuilder withTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
        return this;
    }

    public HttpClientBuilder withListener(IMessageActor<HttpRequest, HttpResponse> listener) {
        this.listener = listener;
        return this;
    }
    
    public HttpClient build() {
        if(timeoutInSeconds == null) throw new RuntimeException("destinationUri not defined");
        HttpClient httpClient = new HttpClient(timeoutInSeconds);
        if(listener != null) httpClient.setListener(listener);
        return httpClient;
    }
}