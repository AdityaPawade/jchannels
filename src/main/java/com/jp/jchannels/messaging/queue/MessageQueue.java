package com.jp.jchannels.messaging.queue;

public interface MessageQueue<I> {

    void registerTopic(String topic);
    
    void addListener(String topic, MessageListener<I> listener);
    
    void pushMessage(String topic, I message);
}
