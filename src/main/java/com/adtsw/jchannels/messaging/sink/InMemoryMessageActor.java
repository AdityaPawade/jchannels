package com.adtsw.jchannels.messaging.sink;

import java.util.HashMap;
import java.util.Map;

public class InMemoryMessageActor<I, O> implements IMessageActor<I, O> {

    private final Map<String, AbstractMessageResponder<I, O>> responders = new HashMap<>();

    public void setResponder(String topic, AbstractMessageResponder<I, O> responder) {
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
