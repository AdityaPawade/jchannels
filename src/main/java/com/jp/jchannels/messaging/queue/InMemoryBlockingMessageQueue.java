package com.jp.jchannels.messaging.queue;

public class InMemoryBlockingMessageQueue<I> extends InMemoryMessageQueue<I> {

    public InMemoryBlockingMessageQueue(String queueName) {
        super(queueName, 1);
    }
}
