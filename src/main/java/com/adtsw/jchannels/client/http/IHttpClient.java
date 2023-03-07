package com.adtsw.jchannels.client.http;

import java.util.Map;

import com.adtsw.jchannels.messaging.sink.IMessageActor;
import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.HttpResponse;

public interface IHttpClient {

    void setListener(IMessageActor<HttpRequest, HttpResponse> messageActor);

    HttpResponse get(String uri, Map<String, String> headers, Map<String, String> cookies);

    HttpResponse post(String uri, String body, Map<String, String> headers, Map<String, String> cookies);

    void shutdown();
}
