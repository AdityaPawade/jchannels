package com.adtsw.jchannels.client.ws;

import com.adtsw.jchannels.server.ws.AbstractSocket;
import com.adtsw.jchannels.utils.HttpUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class WebSocketClient {

    private static final Logger logger = LogManager.getLogger(WebSocketClient.class);
    private Thread clientThread;
    private final String destUri;
    private final Integer connectionTimeoutInSeconds;
    private final org.eclipse.jetty.websocket.client.WebSocketClient client;
    private final WebSocketFactory socketFactory;
    private AbstractSocket socket;
    private final Map<String, String> headers;
    private final Map<String, String> cookies;
    
    public static WebSocketClientBuilder getBuilder() {
        return new WebSocketClientBuilder();
    }

    public WebSocketClient(String destUri, Integer timeoutInSeconds, WebSocketFactory socketFactory,
                           Map<String, String> headers, Map<String, String> cookies) {
        this.connectionTimeoutInSeconds = timeoutInSeconds;
        this.destUri = destUri;
        this.socketFactory = socketFactory;
        this.headers = headers;
        this.cookies = cookies;
        this.client = new org.eclipse.jetty.websocket.client.WebSocketClient();
    }

    public void start(boolean autoReconnect) {
        start(true, false);
    }

    public void start(boolean runInBackground, boolean autoReconnect) {

        if(clientThread != null) {

            logger.warn("Client is already running. Will not attempt connection");
        } else {

            clientThread = new Thread(() -> {
                try {
                    connect();
                    if(autoReconnect) {
                        logger.warn("Attempting ws client reconnection");
                        Thread.sleep(1000);
                        while (true) {
                            connect();
                            Thread.sleep(1000);
                        }
                    } else {
                        logger.warn("Ws client connection terminated");
                    }
                } catch (InterruptedException e) {
                    logger.error("Thread interrupted. shutting down");
                } catch (Exception e) {
                    logger.error("Exception while running server", e);
                }
            });

            clientThread.start();
            
            if(!runInBackground) {
                try {
                    clientThread.join();
                    client.stop();
                } catch (Exception e) {
                    logger.error("Client Thread interrupted. shutting down");
                }
            }
        }
    }

    private void connect() throws Exception {

        client.setStopAtShutdown(true);
        client.start();

        URI uri = new URI(destUri);
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        
        HttpUtil.addCookiesToHeader(cookies, headers);
        headers.forEach(request::setHeader);
         
        socket = socketFactory.createSocket();
        Future<Session> sessionFuture = client.connect(socket, uri, request);
        
        logger.warn(String.format("Connecting to : %s%n", uri));
        // wait for closed socket connection.

        if(connectionTimeoutInSeconds != null) {
            socket.awaitClose(connectionTimeoutInSeconds, TimeUnit.SECONDS);
        } else {
            socket.awaitClose();
        }
    }
    
    public boolean isOpen() {
        return socket != null && socket.isOpen();
    }

    public void shutdown() {
        if(clientThread != null) {
            clientThread.interrupt();
            try {
                client.stop();
            } catch (Exception e) {
                logger.error("Exception while stopping client", e);
            }
        }
    }
}