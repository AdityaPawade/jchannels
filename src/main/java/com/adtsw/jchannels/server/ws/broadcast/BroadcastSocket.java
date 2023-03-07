package com.adtsw.jchannels.server.ws.broadcast;

import com.adtsw.jchannels.server.ws.AbstractSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.ArrayList;
import java.util.List;

@WebSocket
public class BroadcastSocket extends AbstractSocket {

    private static final Logger logger = LogManager.getLogger(BroadcastSocket.class);

    private final Broadcaster broadcaster;
    private final List<String> subscribedTopics;

    public BroadcastSocket(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
        this.subscribedTopics = new ArrayList<>();
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if(this.getSession() != null) {
            subscribedTopics.forEach(topic -> broadcaster.removeSubscription(topic, getSession()));
            getSession().close(StatusCode.NORMAL, "closing session");
            broadcaster.closeSubscription();
            removeSession();
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        boolean allowed = broadcaster.allowSubscription();
        setSession(session);
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        subscribedTopics.add(message);
        broadcaster.subscribe(message, getSession());
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        subscribedTopics.forEach(topic -> broadcaster.removeSubscription(topic, getSession()));
        logger.error("WebSocket Error : " + cause.getMessage(), cause);
    }
}