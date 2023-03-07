package com.adtsw.jchannels.server.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.adtsw.jchannels.messaging.queue.IMessageQueue;
import com.adtsw.jchannels.model.WebSocketMessage;
import com.adtsw.jcommons.utils.CompressionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.List;

@WebSocket(maxTextMessageSize = 100 * 64 * 1024)
public class SubscriptionSocket extends AbstractSocket {

    private static final Logger logger = LogManager.getLogger(SubscriptionSocket.class);

    private final List<String> subscriptionTopics;
    private final IMessageQueue<String> messageQueue;
    private final boolean isPayloadCompressed;

    private static final ObjectMapper mapper = new ObjectMapper();

    public SubscriptionSocket(List<String> subscriptionTopics, 
                              IMessageQueue<String> messageQueue, boolean isPayloadCompressed) {

        super(1, 30000);
        this.subscriptionTopics = subscriptionTopics;
        this.messageQueue = messageQueue;
        this.isPayloadCompressed = isPayloadCompressed;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {

        logger.warn(String.format("Connection closed: %d - %s%n", statusCode, reason));
        close();
        setSession(null);
        this.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {

        logger.info(String.format("Got connect: %s%n", session));
        setSession(session);
        
        try {
            for (String topic : subscriptionTopics) {
                session.getRemote().sendString(topic);
            }
        } catch (Throwable t) {
            throw new RuntimeException("Error establishing connection " + t.getMessage(), t);
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {

        try {
            WebSocketMessage wsMessage = mapper.readValue(message, WebSocketMessage.class);
            String socketMessage = wsMessage.getMessage();
            if(isPayloadCompressed) {
                socketMessage = CompressionUtil.decompress(socketMessage);
            }
            this.messageQueue.pushMessage(wsMessage.getTopic(), socketMessage);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing message " + message, e);
        } catch (Exception e) {
            throw new RuntimeException("Error decompressing message " + message, e);
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.error("WebSocket Error : " + cause.getMessage());
        this.countDown();
    }
    
    public void close() {
        getSession().close(StatusCode.NORMAL, "shutting down");
    }
}