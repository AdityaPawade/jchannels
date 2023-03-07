package com.adtsw.jchannels.server.ws;

import com.adtsw.jchannels.messaging.queue.AbstractMessageListener;
import com.adtsw.jchannels.messaging.queue.IMessageQueue;
import com.adtsw.jchannels.model.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket(maxTextMessageSize = 500 * 64 * 1024)
public class Socket extends AbstractSocket {

    private static final Logger logger = LogManager.getLogger(Socket.class);

    private final IMessageQueue<String> messageQueue;
    private final AbstractMessageListener<String> messageListener;

    public Socket(IMessageQueue<String> messageQueue, long idleTimeoutMs) {
        super(1, idleTimeoutMs);
        this.messageQueue = messageQueue;
        this.messageQueue.registerTopic(Constants.WS.RESPONSE_TOPIC);
        this.messageQueue.registerTopic(Constants.WS.OPS_TOPIC);
        this.messageListener = new AbstractMessageListener<>() {
            @Override
            public void onMessage(String message) {
                try {
                    if (getSession() != null && getSession().isOpen()) {
                        getSession().getRemote().sendString(message);
                    } else {
                        logger.warn("Could not send ws message as connection was not established");
                    }
                } catch (Throwable t) {
                    throw new RuntimeException("Error sending ws message " + t.getMessage(), t);
                }
            }
        };
        this.messageQueue.addListener(Constants.WS.REQUEST_TOPIC, messageListener);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.warn(String.format("Connection closed: %d - %s%n", statusCode, reason));
        close();
        removeSession();
        this.messageQueue.removeListener(Constants.WS.REQUEST_TOPIC, messageListener);
        this.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info(String.format("Got connect: %s%n", session));
        setSession(session);
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        this.messageQueue.pushMessage(Constants.WS.RESPONSE_TOPIC, message);
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.error("WebSocket Error : " + cause.getMessage());
        this.messageQueue.removeListener(Constants.WS.REQUEST_TOPIC, messageListener);
        this.countDown();
    }
    
    public void close() {
        getSession().close(StatusCode.NORMAL, "shutting down");
    }
}