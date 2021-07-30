package com.jp.jchannels.client.http;

import com.jp.jchannels.messaging.sink.MessageActor;
import com.jp.jchannels.messaging.sink.MessageResponder;
import com.jp.jchannels.model.Constants;
import com.jp.jchannels.model.HttpRequest;
import com.jp.jchannels.model.HttpResponse;
import com.jp.jchannels.model.exception.HttpException;
import com.jp.utils.HttpUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.InputStream;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpClient {

    private static final Logger logger = LogManager.getLogger(HttpClient.class);
    private final Integer timeout;
    private final org.eclipse.jetty.client.HttpClient client;

    public static HttpClientBuilder getBuilder() {
        return new HttpClientBuilder();
    }

    public HttpClient(Integer timeoutInSeconds, MessageActor<HttpRequest, HttpResponse> messageActor) {

        this.timeout = timeoutInSeconds;
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        this.client = new org.eclipse.jetty.client.HttpClient(sslContextFactory);
        try {
            client.start();
        } catch (Exception e) {
            logger.error("Exception while starting client", e);
            throw new RuntimeException(e);
        }

        setResponders(messageActor);
    }

    private void setResponders(MessageActor<HttpRequest, HttpResponse> messageActor) {

        messageActor.setResponder(Constants.GET_TOPIC, new MessageResponder<>() {
            @Override
            public HttpResponse onMessage(HttpRequest message) {
                return get(message.getUri(), message.getHeaders(), message.getCookies());
            }
        });

        messageActor.setResponder(Constants.POST_TOPIC, new MessageResponder<>() {
            @Override
            public HttpResponse onMessage(HttpRequest message) {
                return post(message.getUri(), message.getBody(), message.getHeaders(), message.getCookies());
            }
        });
    }

    public HttpResponse get(String uri, Map<String, String> headers, Map<String, String> cookies) {

        Request request = client.newRequest(uri);
        request.method(HttpMethod.GET);
        return getResponse(headers, cookies, request);
    }

    public HttpResponse post(String uri, String body, Map<String, String> headers, Map<String, String> cookies) {

        Request request = client.newRequest(uri);
        request.method(HttpMethod.POST);
        request.content(new StringContentProvider(body));
        return getResponse(headers, cookies, request);
    }

    private HttpResponse getResponse(Map<String, String> headers, Map<String, String> cookies, Request request) {

        try {
            request.agent(null);
            client.getCookieStore().removeAll();
            HttpUtil.addCookiesToHeader(cookies, headers);
            headers.forEach(request::header);
            request.timeout(timeout, TimeUnit.SECONDS);

            InputStreamResponseListener listener = new InputStreamResponseListener();
            request.send(listener);
            org.eclipse.jetty.client.HttpResponse response = (org.eclipse.jetty.client.HttpResponse) listener.get(
                10, TimeUnit.SECONDS
            );
            
            InputStream inputStream = listener.getInputStream();

            String content = null;
            if(inputStream != null) {
                content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }

            if (response.getStatus() != HttpStatus.OK_200) {
                String errorReason = StringUtils.isEmpty(content) ? response.getReason() : content;
                throw new HttpException(response.getStatus(), errorReason);
            }

            HttpFields headerFields = response.getHeaders();
            Map<String, String> responseHeaders = HttpUtil.extractHeaders(headerFields);
            Map<String, String> responseCookies = HttpUtil.extractCookies(headerFields);
            return new HttpResponse(content, responseHeaders, responseCookies);

        } catch (HttpException e) {
            logger.error("Exception in getting HTTP response : [ " + e.getStatusCode() + " ], " + e.getReason());
            return new HttpResponse(e);
        } catch (Exception e) {
            logger.error("Exception in HTTP request", e);
            throw new RuntimeException("Exception in HTTP request", e);
        }
    }

    public void shutdown() {
        try {
            client.stop();
        } catch (Exception e) {
            logger.error("Exception while stopping client", e);
        }
    }
}