package com.adtsw.jchannels.messaging.queue;

public class InMemoryBlockingMessageQueue<I> extends InMemoryMessageQueue<I> {

    public InMemoryBlockingMessageQueue(String queueName) {
        this(queueName, 1, QueueFullAction.BLOCK);
    }

    public InMemoryBlockingMessageQueue(String queueName, QueueFullAction queueFullAction) {
        this(queueName, 1, queueFullAction);
    }
    
    public InMemoryBlockingMessageQueue(String queueName, int numPartitions, QueueFullAction queueFullAction) {
        super(queueName, numPartitions, 1, queueFullAction);
    }
}
