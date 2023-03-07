package com.adtsw.jchannels.server.ws.broadcast;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

public class BroadcastSocketCreator implements WebSocketCreator {

    private final Broadcaster broadcaster;
    private final String path;

    public BroadcastSocketCreator(Broadcaster broadcaster, String path) {
        this.broadcaster = broadcaster;
        this.path = path;
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        
        if(req.getHttpServletRequest().getPathInfo().equals(path)) {
            return new BroadcastSocket(broadcaster);
        }
        
        return null;
    }
}
