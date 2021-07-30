package com.jp.jchannels.client.ws;

import com.jp.jchannels.server.ws.AbstractSocket;

public abstract class WebSocketFactory {

    public abstract AbstractSocket createSocket();
}