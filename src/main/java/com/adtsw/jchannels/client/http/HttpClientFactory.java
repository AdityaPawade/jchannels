package com.adtsw.jchannels.client.http;

import com.adtsw.jchannels.messaging.sink.IMessageActor;
import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.HttpResponse;

public class HttpClientFactory {
    
    public HttpClient getClient(int timeoutInSeconds, IMessageActor<HttpRequest, HttpResponse> listener) {

        return HttpClient.getBuilder()
            .withTimeoutInSeconds(timeoutInSeconds)
            .withListener(listener)
            .build();
    }
}
