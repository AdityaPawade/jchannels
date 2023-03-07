package com.adtsw.jchannels.server;

import com.adtsw.jchannels.auth.IAuthorizer;
import com.adtsw.jchannels.messaging.message.BroadcastMessage;
import com.adtsw.jchannels.messaging.queue.IMessageQueue;
import com.adtsw.jchannels.messaging.sink.IMessageActor;
import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.HttpResponse;
import com.adtsw.jchannels.server.http.HttpServlet;
import com.adtsw.jchannels.server.ws.WSTopic;
import com.adtsw.jchannels.server.ws.broadcast.BroadcastSocket;
import com.adtsw.jchannels.server.ws.broadcast.Broadcaster;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class WebServerBuilder {

    private static Logger logger = LogManager.getLogger(WebServerBuilder.class);
    
    private Integer port;
    private QueuedThreadPool threadPool;
    private ResourceHandlerDetails resourceHandler;
    private final List<ServletHolderDetails> servletHolders = new ArrayList<>();
    private final HandlerList handlers;
    private String requestOriginServer;
    private String error404URI;
    private String error500URI;
    
    WebServerBuilder() {
        handlers = new HandlerList();
    }
    
    public WebServerBuilder withPort(int port) {
        this.port = port;
        return this;
    }
    
    public WebServerBuilder withThreadPool(int maxThreads, int minThreads, int idleTimeout) {
        this.threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        return this;
    }

    public WebServerBuilder withBroadcastHandler(String path, List<WSTopic> topics, 
                                                 IMessageQueue<BroadcastMessage<String>> gameMessageQueue,
                                                 boolean compressPayload, int connectionLimit) {

        Broadcaster broadcaster = new Broadcaster(topics, gameMessageQueue, compressPayload, connectionLimit);

        WebSocketHandler handler = new WebSocketHandler() {
            @Override public void configure(WebSocketServletFactory factory) {
            factory.setCreator((ServletUpgradeRequest req, ServletUpgradeResponse resp) -> {
                if(req.getHttpServletRequest().getPathInfo().equals(path)) {
                    if(broadcaster.allowSubscription()) {
                        return new BroadcastSocket(broadcaster);
                    } else {
                        return null;
                    }
                }
                return null;
            });
            }
        };

        this.handlers.addHandler(handler);
        return this;
    }
    
    public WebServerBuilder withResourceHandler(String path, String resourceBase) {
                                                
        this.resourceHandler = new ResourceHandlerDetails(path, resourceBase);
        return this;
    }

    public WebServerBuilder withServletHandler(String path, Servlet servlet) {

        servletHolders.add(new ServletHolderDetails(new ServletHolder(servlet), path));
        return this;
    }

    public WebServerBuilder withServletHandler(String path, IAuthorizer authorizer, 
                                               String scope, IMessageActor<HttpRequest, HttpResponse> messageActor) {

        HttpServlet servlet = new HttpServlet(path, authorizer, scope, scope, messageActor);
        servletHolders.add(new ServletHolderDetails(new ServletHolder(servlet), path));
        return this;
    }

    public WebServerBuilder withServletHandler(String path, IAuthorizer authorizer,
                                               String getScope, String postScope, 
                                               IMessageActor<HttpRequest, HttpResponse> messageActor) {

        HttpServlet servlet = new HttpServlet(path, authorizer, getScope, postScope, messageActor);
        servletHolders.add(new ServletHolderDetails(new ServletHolder(servlet), path));
        return this;
    }

    public WebServerBuilder withServletHandler(String path, IMessageActor<HttpRequest, HttpResponse> messageActor) {

        HttpServlet servlet = new HttpServlet(path, messageActor);
        servletHolders.add(new ServletHolderDetails(new ServletHolder(servlet), path));
        return this;
    }

    public WebServerBuilder withCorsFilter(String requestOriginServer) {

        this.requestOriginServer = requestOriginServer;
        return this;
    }

    public WebServerBuilder withError404URI(String error404URI) {

        this.error404URI = error404URI;
        return this;
    }

    public WebServerBuilder withError500URI(String error500URI) {

        this.error500URI = error500URI;
        return this;
    }

    private void createResourceHandler() {
        
        if(this.resourceHandler != null) {
        
            ContextHandler handler = new ContextHandler(this.resourceHandler.getPath());
            handler.setResourceBase(this.resourceHandler.getResourceBase());
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(false);
            resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
            handler.setHandler(resourceHandler);
            addedErrorHandler(handler);
            this.handlers.addHandler(handler);
        }
    }
    
    private void createServletHandler() {
    
        if(this.servletHolders.size() > 0) {
            ServletContextHandler handler = new ServletContextHandler();
            
            this.servletHolders.forEach(holder -> {
                ServletHolder servletHolder = holder.getServletHolder();
                handler.addServlet(servletHolder, holder.getPath());
            });
    
            if(!StringUtils.isEmpty(this.requestOriginServer)) {
                addCorsFilter(this.requestOriginServer, handler);
            }
            handler.getServletHandler().setEnsureDefaultServlet(false);
            addedErrorHandler(handler);
            this.handlers.addHandler(handler);
        }
    }

    private void addedErrorHandler(ContextHandler handler) {
        
        ErrorPageErrorHandler errorMapper = new ErrorPageErrorHandler();
        if(StringUtils.isNotEmpty(this.error404URI)) {
            errorMapper.addErrorPage(404, this.error404URI);
        }
        if(StringUtils.isNotEmpty(this.error500URI)) {
            errorMapper.addErrorPage(500, this.error500URI);
        }
        handler.setErrorHandler(errorMapper);
    }

    private void addCorsFilter(String requestOiginServer, ServletContextHandler handler) {
        FilterHolder holder = new FilterHolder(CrossOriginFilter.class);
        holder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        holder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, requestOiginServer);
        holder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
        holder.setInitParameter(
            CrossOriginFilter.ALLOWED_HEADERS_PARAM, 
            "X-Requested-With,Content-Type,Accept,Origin,Authorization"
        );
        holder.setName("cross-origin");
        handler.addFilter(holder, "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    public WebServer build() {
        if(port == null) throw new RuntimeException("port not defined");
        if(threadPool == null) throw new RuntimeException("threadPool not defined");
        createServletHandler();
        createResourceHandler();
        return new WebServer(port, threadPool, handlers);
    }
}

@AllArgsConstructor
@Getter
class ServletHolderDetails {
    @Getter
    private final ServletHolder servletHolder;
    private final String path;
}
@AllArgsConstructor
@Getter
class ResourceHandlerDetails {
    private final String path;
    private final String resourceBase;
}