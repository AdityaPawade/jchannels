package com.adtsw.jchannels.client.ws;

import com.adtsw.jchannels.messaging.queue.MessageQueue;
import com.adtsw.jchannels.server.ws.AbstractSocket;
import com.adtsw.jchannels.server.ws.Socket;
import com.adtsw.jchannels.server.ws.SubscriptionSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketClientBuilder {

    private static Logger logger = LogManager.getLogger(WebSocketClientBuilder.class);
    
    private String destinationUri;
    private Integer connectionTimeoutInSeconds = null;
    private Integer idleTimeoutInSeconds = null;
    private WebSocketFactory socketFactory;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private MessageQueue<String> messageQueue;
    
    WebSocketClientBuilder() {
    }
    
    public WebSocketClientBuilder withDestinationUri(String destinationUri) {
        this.destinationUri = destinationUri;
        return this;
    }

    public WebSocketClientBuilder withConnectionTimeoutInSeconds(int timeoutInSeconds) {
        this.connectionTimeoutInSeconds = timeoutInSeconds;
        return this;
    }

    public WebSocketClientBuilder withIdleTimeoutInSeconds(int timeoutInSeconds) {
        this.idleTimeoutInSeconds = timeoutInSeconds;
        return this;
    }

    public WebSocketClientBuilder withSocket(MessageQueue<String> messageQueue) {
        this.messageQueue = messageQueue;
        return this;
    }

    public WebSocketClientBuilder withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public WebSocketClientBuilder withCookies(Map<String, String> cookies) {
        this.cookies = cookies;
        return this;
    }

    public WebSocketClientBuilder withSubscriptionSocket(List<String> subscriptionTopics, 
                                                         MessageQueue<String> messageQueue,
                                                         boolean isPayloadCompressed) {
        this.socketFactory = new WebSocketFactory() {
            @Override
            public AbstractSocket createSocket() {
                return new SubscriptionSocket(subscriptionTopics, messageQueue, isPayloadCompressed);
            }
        };
        return this;
    }
    
    public WebSocketClient build() {
        if(destinationUri == null) throw new RuntimeException("destinationUri not defined");
        if(socketFactory == null) throw new RuntimeException("socket not defined");
        if(headers == null) headers = new HashMap<>();
        if(cookies == null) cookies = new HashMap<>();
        if(connectionTimeoutInSeconds == null) connectionTimeoutInSeconds = 5;
        if(idleTimeoutInSeconds == null) idleTimeoutInSeconds = 300000;
        this.socketFactory = new WebSocketFactory() {
            @Override
            public AbstractSocket createSocket() {
                return new Socket(messageQueue, idleTimeoutInSeconds);
            }
        };
        return new WebSocketClient(destinationUri, connectionTimeoutInSeconds, socketFactory, headers, cookies);
    }
}