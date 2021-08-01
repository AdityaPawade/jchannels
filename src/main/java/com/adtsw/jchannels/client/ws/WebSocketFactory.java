package com.adtsw.jchannels.client.ws;

import com.adtsw.jchannels.server.ws.AbstractSocket;

public abstract class WebSocketFactory {

    public abstract AbstractSocket createSocket();
}