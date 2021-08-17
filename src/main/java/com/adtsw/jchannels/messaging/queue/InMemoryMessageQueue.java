package com.adtsw.jchannels.messaging.queue;

import com.adtsw.jcommons.ds.BlockingThreadPoolExecutor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryMessageQueue<I> implements MessageQueue<I> {

    private static final Logger logger = LogManager.getLogger(InMemoryMessageQueue.class);
    private final Map<String, List<MessageListener<I>>> listeners = new HashMap<>();
    private final BlockingThreadPoolExecutor executorService;
    private final String queueName;
    
    public InMemoryMessageQueue(String queueName, int threadPoolSize) {
        this.executorService = new BlockingThreadPoolExecutor(queueName, threadPoolSize, threadPoolSize);
        this.queueName = queueName;
    }

    public InMemoryMessageQueue(String queueName) {
        this(queueName, 10);
    }

    public void registerTopic(String topic) {
        if(!listeners.containsKey(topic)) {
            listeners.put(topic, new ArrayList<>());
        }
    }

    public void addListener(String topic, MessageListener<I> listener) {
        registerTopic(topic);
        listeners.get(topic).add(listener);
    }

    @Override
    public void removeListener(String topic, MessageListener<I> listener) {
        listeners.get(topic).remove(listener);
    }

    public void pushMessage(String topic, I message) {
        try {
            executorService.executeButBlockIfFull(() -> {
                List<MessageListener<I>> messageListeners = listeners.get(topic);
                if(CollectionUtils.isNotEmpty(messageListeners)) {
                    messageListeners.forEach(listener -> listener.onMessage(message));
                } else {
                    logger.warn("Sending message to topic without listener " + topic);
                }
            });
        } catch (InterruptedException e) {
            logger.warn(queueName + " - Push message task interrupted " + e.getMessage());
        }
    }
}
