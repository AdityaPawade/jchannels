package com.adtsw.jchannels.messaging.queue;

import com.adtsw.jchannels.messaging.sink.IMessageActor;
import com.adtsw.jchannels.model.Constants;
import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.HttpRequest.HttpRequestBuilder;
import com.adtsw.jchannels.model.HttpResponse;

public class HttpMessageListener extends AbstractMessageListener<String> {

    private final IMessageActor<HttpRequest, HttpResponse> httpActor;
    private final String uri;

    public HttpMessageListener(IMessageActor<HttpRequest, HttpResponse> httpActor, String uri) {
        this.httpActor = httpActor;
        this.uri = uri;
    }

    @Override
    public void onMessage(String message) {
        HttpRequest request = (new HttpRequestBuilder())
            .withUri(uri)
            .withBody(message)
            .build();
        httpActor.getResponse(Constants.POST_TOPIC, request);
    }
}
