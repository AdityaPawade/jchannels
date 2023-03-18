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
    private final Integer maxConnectionRetries;
    private final Boolean autoReconnectOnTermination;
    private final org.eclipse.jetty.websocket.client.WebSocketClient client;
    private final WebSocketFactory socketFactory;
    private AbstractSocket socket;
    private final Map<String, String> headers;
    private final Map<String, String> cookies;
    
    public static WebSocketClientBuilder getBuilder() {
        return new WebSocketClientBuilder();
    }

    public WebSocketClient(String destUri, 
                           Integer connectionTimeoutInSeconds, Integer maxConnectionRetries, 
                           Boolean autoReconnectOnTermination, WebSocketFactory socketFactory,
                           Map<String, String> headers, Map<String, String> cookies) {

        this.connectionTimeoutInSeconds = connectionTimeoutInSeconds;
        this.maxConnectionRetries = maxConnectionRetries; 
        this.autoReconnectOnTermination = autoReconnectOnTermination;
        this.destUri = destUri;
        this.socketFactory = socketFactory;
        this.headers = headers;
        this.cookies = cookies;
        this.client = new org.eclipse.jetty.websocket.client.WebSocketClient();
    }
    
    public boolean start(boolean runInBackground) {
        startConnectionThread(runInBackground);
        int currentRetryAttempt = 0;
        boolean attemptingConnection = true;
        while(attemptingConnection && !isOpen()) {
            logger.info("Waiting for WS client to connect.... attempt " + currentRetryAttempt + " / " + maxConnectionRetries);
            try {
                Thread.sleep(1000);
                currentRetryAttempt = currentRetryAttempt + 1;
                if(currentRetryAttempt > maxConnectionRetries) {
                    shutdown();
                    attemptingConnection = false;
                    logger.warn("Max connection wait time for WS client exhausted");
                }
            } catch (InterruptedException e) {
                logger.warn("WS client connection thread interrupted");
                return false;
            }
        }
        return attemptingConnection;
    }

    private void startConnectionThread(boolean runInBackground) {

        if(clientThread != null) {
            logger.warn("Client is already running. Will not attempt connection");
        } else {
            clientThread = getClientConnectionThread();
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

    private Thread getClientConnectionThread() {
        return new Thread(() -> {
            try {
                connect();
                if(autoReconnectOnTermination) {
                    logger.warn("Checking for ws client reconnection");
                    Thread.sleep(1000);
                    while (true) {
                        if(!isOpen()) {
                            connect();
                            Thread.sleep(connectionTimeoutInSeconds * 1000);
                        }
                    }
                } else {
                    logger.warn("WS client connection terminated");
                }
            } catch (InterruptedException e) {
                logger.error("Thread interrupted. shutting down");
            } catch (Exception e) {
                logger.error("Exception while running server", e);
            }
        }, "web-socket-client-connection-thread");
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
                logger.error("Exception while stopping WS client", e);
            }
        }
    }
}