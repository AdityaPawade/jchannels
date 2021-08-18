package com.adtsw.jchannels.messaging.queue;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

public class MessageQueueStats {
    
    @Getter
    private final Map<String, Integer> statistics;

    public MessageQueueStats() {
        this.statistics = new TreeMap<>();
    }
    
    public void add(String id, Integer value) {
        statistics.put(id, value);
    }
}
