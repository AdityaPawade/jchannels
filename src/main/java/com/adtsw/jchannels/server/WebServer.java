package com.adtsw.jchannels.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.ThreadPool;

public class WebServer {

    private static Logger logger = LogManager.getLogger(WebServer.class);
    private Thread serverThread;
    private final Server server;
    private final ServerConnector connector;
    
    public static WebServerBuilder getBuilder() {
        return new WebServerBuilder();
    }

    public WebServer(int port, ThreadPool threadPool, Handler handler) {
        server = new Server(threadPool);
        connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);
        server.setHandler(handler);
    }

    public void start() {
        start(true);
    }

    public void start(boolean runInBackground) {
        if(serverThread != null) {
            logger.warn("Server is already running. Will not restart");
        } else {
            serverThread = new Thread(() -> {
                try {
                    server.start();
                    String host = connector.getHost();
                    if (host == null) {
                        host = "localhost";
                    }
                    int port = connector.getLocalPort();
                    logger.info(String.format("Web Server started on %s:%d",host,port));
                    server.join();
                } catch (InterruptedException e) {
                    logger.error("Thread interrupted. shutting down");
                } catch (Exception e) {
                    logger.error("Exception while running server", e);
                }
            });
            serverThread.start();
            
            if(!runInBackground) {
                try {
                    serverThread.join();
                } catch (InterruptedException e) {
                    logger.error("Thread interrupted. shutting down");
                }
            }
        }
    }
    
    public void shutdown() {
        if(serverThread != null) {
            serverThread.interrupt();
            try {
                server.stop();
            } catch (Exception e) {
                logger.error("Exception while stopping server", e);
            }
        }
    }
}