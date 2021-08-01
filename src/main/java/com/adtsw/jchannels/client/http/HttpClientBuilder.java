package com.adtsw.jchannels.client.http;

import com.adtsw.jchannels.messaging.sink.MessageActor;
import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpClientBuilder {

    private static Logger logger = LogManager.getLogger(HttpClientBuilder.class);
    
    private Integer timeoutInSeconds = null;
    private MessageActor<HttpRequest, HttpResponse> messageActor;
    
    HttpClientBuilder() {
    }
    
    public HttpClientBuilder withTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
        return this;
    }

    public HttpClientBuilder withMessageActor(MessageActor<HttpRequest, HttpResponse> messageActor) {
        this.messageActor = messageActor;
        return this;
    }
    
    public HttpClient build() {
        if(timeoutInSeconds == null) throw new RuntimeException("destinationUri not defined");
        if(messageActor == null) throw new RuntimeException("socket not defined");
        return new HttpClient(timeoutInSeconds, messageActor);
    }
}