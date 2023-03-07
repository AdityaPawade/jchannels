package com.adtsw.jchannels.messaging.queue;

public interface IMessageQueue<I> {

    void registerTopic(String topic);
    
    void addListener(String topic, AbstractMessageListener<I> listener);
    
    void removeListener(String topic, AbstractMessageListener<I> listener);
    
    boolean pushMessage(String topic, I message);
    
    boolean pushMessage(String topic, I message, int partition);

    MessageQueueStats getStats();

    void shutdown();
}
