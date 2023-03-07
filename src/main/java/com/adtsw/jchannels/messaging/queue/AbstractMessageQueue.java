package com.adtsw.jchannels.messaging.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Logger;

public abstract class AbstractMessageQueue<I> implements IMessageQueue<I> {
    
    private final Map<String, List<AbstractMessageListener<I>>> listeners = new HashMap<>();

    @Override
    public void registerTopic(String topic) {
        if(!listeners.containsKey(topic)) {
            listeners.put(topic, new ArrayList<>());
        }
    }

    @Override
    public void addListener(String topic, AbstractMessageListener<I> listener) {
        registerTopic(topic);
        listeners.get(topic).add(listener);
    }

    @Override
    public void removeListener(String topic, AbstractMessageListener<I> listener) {
        listeners.get(topic).remove(listener);
    }

    protected void pushMessageToListeners(String topic, I message) {
        List<AbstractMessageListener<I>> messageListeners = listeners.get(topic);
        if(CollectionUtils.isNotEmpty(messageListeners)) {
            messageListeners.forEach(listener -> listener.onMessage(message));
        } else {
            getLogger().warn("Sending message to topic without listener " + topic);
        }
    }

    protected abstract Logger getLogger();
}
