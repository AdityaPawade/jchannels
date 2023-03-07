package com.adtsw.jchannels.messaging.queue;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adtsw.jchannels.client.http.HttpClient;
import com.adtsw.jchannels.client.http.HttpClientBuilder;
import com.adtsw.jchannels.client.http.HttpClientFactory;
import com.adtsw.jchannels.messaging.sink.IMessageActor;
import com.adtsw.jchannels.messaging.sink.InMemoryMessageActor;
import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.HttpResponse;

import lombok.Getter;

public class HttpMessageQueue extends InMemoryMessageQueue<String> {
    
    @Getter
    private final Logger logger = LogManager.getLogger(InMemoryMessageQueue.class);

    private final IMessageActor<HttpRequest, HttpResponse> httpActor;

    public HttpMessageQueue(HttpClientFactory httpClientFactory, String queueName, 
        int numPartitions, int threadPoolSizePerPartition,
        QueueFullAction queueFullAction) {
        this(
            httpClientFactory, queueName, numPartitions, threadPoolSizePerPartition, 
            queueFullAction, 1, TimeUnit.SECONDS
        );
    }

    public HttpMessageQueue(HttpClientFactory httpClientFactory, String queueName, 
        int numPartitions, int threadPoolSizePerPartition,
        QueueFullAction queueFullAction, long timeout, TimeUnit unit) {
        super(queueName, numPartitions, threadPoolSizePerPartition, queueFullAction, timeout, unit);
        this.httpActor = new InMemoryMessageActor<>();
        HttpClientBuilder httpClientBuilder = HttpClient.getBuilder();
        httpClientBuilder
            .withTimeoutInSeconds(5)
            .withListener(httpActor)
            .build();
    }

    public HttpMessageQueue(HttpClientFactory httpClientFactory, String queueName, 
        QueueFullAction queueFullAction) {
        this(
            httpClientFactory, queueName, 1,
            10, queueFullAction
        );
    }

    public HttpMessageQueue(HttpClientFactory httpClientFactory, String queueName) {
        this(
            httpClientFactory, queueName, 1, 
            10, QueueFullAction.BLOCK
        );
    }

    public void addListener(String topic, String uri) {
        addListener(topic, new HttpMessageListener(httpActor, uri));
    }
}
