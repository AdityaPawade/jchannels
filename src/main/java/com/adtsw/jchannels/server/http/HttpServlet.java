package com.adtsw.jchannels.server.http;

import com.adtsw.jchannels.auth.IAuthorizer;
import com.adtsw.jchannels.messaging.sink.IMessageActor;
import com.adtsw.jchannels.model.Constants;
import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.HttpResponse;
import com.adtsw.jchannels.model.auth.SessionInfo;
import com.adtsw.jchannels.model.exception.UnauthorizedAccessException;
import com.adtsw.jchannels.utils.HttpUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class HttpServlet extends javax.servlet.http.HttpServlet {

    private static final Logger logger = LogManager.getLogger(HttpServlet.class);

    private final String apiPath;
    private final IMessageActor<HttpRequest, HttpResponse> messageActor;
    private final Optional<IAuthorizer> authorizer;
    private final String getScope;
    private final String postScope;

    public HttpServlet(String apiPath, IMessageActor<HttpRequest, HttpResponse> messageActor) {

        this.apiPath = apiPath;
        this.messageActor = messageActor;
        this.authorizer = Optional.empty();
        this.getScope = Constants.OPEN_SCOPE;
        this.postScope = Constants.OPEN_SCOPE;
    }

    public HttpServlet(String apiPath, IAuthorizer authorizer,
                       String getScope, String postScope,
                       IMessageActor<HttpRequest, HttpResponse> messageActor) {

        this.apiPath = apiPath;
        this.messageActor = messageActor;
        this.authorizer = Optional.of(authorizer);
        this.getScope = getScope;
        this.postScope = postScope;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        handleRequest(request, response, null, Constants.GET_TOPIC, getScope);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String body = IOUtils.toString(request.getReader());
        handleRequest(request, response, body, Constants.POST_TOPIC, postScope);
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response,
                               String body, String topic, String scope) throws IOException {

        response.setContentType("application/json");

        try {

            String uri = getUri(request);
            String domain = getDomain(request);

            HttpRequest httpRequest = getHttpRequest((Request) request, domain, uri, body, scope);

            HttpResponse actorResponse = messageActor.getResponse(
                topic, httpRequest
            );
            
            actorResponse.getHeaders().forEach(response::setHeader);
            actorResponse.getCookies().forEach((cookieName, cookieValue) -> {
                Cookie cookie = new Cookie(cookieName, cookieValue);
                cookie.setPath("/");
                cookie.setSecure(false);
                cookie.setHttpOnly(false);
                cookie.setComment(HttpCookie.SAME_SITE_STRICT_COMMENT);
                response.addCookie(cookie);
            });

            if(actorResponse.isSuccess()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(actorResponse.getBody());
            } else {
                response.setStatus(actorResponse.getException().getStatusCode());
                response.getWriter().println("{ \"status\": \"error\", \"message\": \"" + actorResponse.getException().getReason() + "\"}");
            }

        } catch (UnauthorizedAccessException ue) {

            logger.error("Unauthorized access for " + request.getPathInfo());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("{ \"status\": \"error\", \"message\": \"" + ue.getMessage() + "\"}");

        } catch (Exception e) {

            logger.error("Exception occurred while fetching response for " + request.getPathInfo(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("{ \"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }

    private String getDomain(HttpServletRequest request) {
        return request.getScheme() + "://" +   // "http" + "://
             request.getServerName() +       // "myhost"
             ":" + request.getServerPort(); // ":" + "8080"
    }

    private String getUri(HttpServletRequest request) {

        String basePath = apiPath;
        while (basePath.charAt(basePath.length() - 1) == '/' || basePath.charAt(basePath.length() - 1) == '*') {
            basePath = basePath.replaceAll("\\*$", "").replaceAll("/$", "");
        }
        StringBuilder sb = new StringBuilder(basePath);
        String pathInfo = request.getPathInfo();
        if(StringUtils.isNotEmpty(pathInfo)) {
            sb.append(pathInfo);
        }
        String queryString = request.getQueryString();
        if(StringUtils.isNotEmpty(queryString)) {
            sb.append("?").append(queryString);
        }
        return sb.toString();
    }

    private HttpRequest getHttpRequest(Request request, String domain, String uri, String body, String scope)
        throws UnauthorizedAccessException {

        HttpFields httpFields = request.getHttpFields();
        Map<String, String> requestHeaders = HttpUtil.extractHeaders(httpFields);
        Map<String, String> requestCookies = HttpUtil.extractCookies(httpFields);
        HttpRequest httpRequest = HttpRequest.getBuilder().
            withDomain(domain).
            withUri(uri).
            withBody(body).
            withHeaders(requestHeaders).
            withCookies(requestCookies).build();
        SessionInfo sessionInfo = this.authorizer.isPresent() ? authorizer.get().authorize(httpRequest, scope): null;
        return HttpRequest.getBuilder(httpRequest).withSessionInfo(sessionInfo).build();
    }
}