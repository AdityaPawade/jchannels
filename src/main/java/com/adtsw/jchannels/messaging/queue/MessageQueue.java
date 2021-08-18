package com.adtsw.jchannels.messaging.queue;

public interface MessageQueue<I> {

    void registerTopic(String topic);
    
    void addListener(String topic, MessageListener<I> listener);
    
    void removeListener(String topic, MessageListener<I> listener);
    
    boolean pushMessage(String topic, I message);
    
    boolean pushMessage(String topic, I message, int partition);

    MessageQueueStats getStats();
}
