package com.jp.jchannels.messaging.sink;

import java.util.HashMap;
import java.util.Map;

public class InMemoryMessageActor<I, O> implements MessageActor<I, O> {

    private final Map<String, MessageResponder<I, O>> responders = new HashMap<>();

    public void setResponder(String topic, MessageResponder<I, O> responder) {
        if(responders.containsKey(topic)) {
            throw new RuntimeException("responder is already registered");
        }
        responders.put(topic, responder); 
    }
    
    public O getResponse(String topic, I message) {
        if(!responders.containsKey(topic)) {
            throw new RuntimeException("responder is not registered");
        }
        return responders.get(topic).onMessage(message);
    }
}
