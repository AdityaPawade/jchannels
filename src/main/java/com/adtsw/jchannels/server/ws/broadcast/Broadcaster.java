package com.adtsw.jchannels.server.ws.broadcast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.adtsw.jchannels.messaging.message.BroadcastMessage;
import com.adtsw.jchannels.messaging.queue.AbstractMessageListener;
import com.adtsw.jchannels.messaging.queue.IMessageQueue;
import com.adtsw.jchannels.model.Constants;
import com.adtsw.jchannels.model.WebSocketMessage;
import com.adtsw.jchannels.rate.CountBasedRateLimiter;
import com.adtsw.jchannels.rate.IRateLimiter;
import com.adtsw.jchannels.server.ws.WSTopic;
import com.adtsw.jcommons.utils.CompressionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Broadcaster {

    private static final Logger logger = LogManager.getLogger(Broadcaster.class);

    private final Map<String, ConcurrentLinkedQueue<Session>> topicSubscriptions = new HashMap<>();
    private final Map<String, String> lastTopicMessage = new HashMap<>();
    private final Map<String, Long> lastTopicMessageBroadcastTime = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private final IRateLimiter rateLimiter;

    public Broadcaster(List<WSTopic> topics, IMessageQueue<BroadcastMessage<String>> messageQueue, 
                       boolean compressPayload, int connectionLimit) {
    
        this.rateLimiter = new CountBasedRateLimiter(connectionLimit);
        
        topics.forEach(topic -> {
            topicSubscriptions.put(topic.getName(), new ConcurrentLinkedQueue<>());
            messageQueue.addListener(topic.getName(), new AbstractMessageListener<>() {
                @Override
                public void onMessage(BroadcastMessage<String> broadcastMessage) {
                    String message = broadcastMessage.getMessage();
                    if(Constants.WS.CLEAR_MESSAGE.equals(message)) {
                        lastTopicMessage.remove(topic.getName());
                    } else {
                        try {
                            if(!topic.shouldThrottle() || allowBroadcast(topic)) {
                                lastTopicMessageBroadcastTime.put(topic.getName(), System.currentTimeMillis());
                                if (compressPayload) {
                                    message = CompressionUtil.compress(message);
                                }
                                String wsMessage = getWsMessage(topic.getName(), message);
                                broadcast(topic.getName(), wsMessage);
                                if (broadcastMessage.isSticky()) {
                                    lastTopicMessage.put(topic.getName(), wsMessage);
                                }
                            }
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("unable to serialise message", e);
                        } catch (IOException e) {
                            throw new RuntimeException("unable to compress message", e);
                        }
                    }
                }
            });
        });
    }

    private boolean allowBroadcast(WSTopic topic) {
        if(!lastTopicMessageBroadcastTime.containsKey(topic.getName())) return true;
        long messageInterval = System.currentTimeMillis() - lastTopicMessageBroadcastTime.get(topic.getName());
        return messageInterval > topic.getMinMessageInterval();
    }
    
    public boolean allowSubscription() {
        boolean allow = rateLimiter.allow();
        if(allow) {
            logger.info("Added subscription to broadcaster. Subscription count is now " + rateLimiter.getRate());
        }
        return allow;
    }

    public void subscribe(String topic, Session session) {
        if(topicSubscriptions.containsKey(topic)) {
            topicSubscriptions.get(topic).add(session);
            if(lastTopicMessage.containsKey(topic)) {
                String message = lastTopicMessage.get(topic);
                sendMessage(message, session);
            }
        }
    }
    
    public void closeSubscription() {
        rateLimiter.exit();
        logger.info("Removed subscription from broadcaster. Subscription count is now " + rateLimiter.getRate());
    }

    public void removeSubscription(String topic, Session session) {
        if (topicSubscriptions.containsKey(topic)) {
            topicSubscriptions.get(topic).remove(session);
        }
    }

    public void broadcast(String topic, String message) {
        for (Session session : topicSubscriptions.get(topic)) {
            sendMessage(message, session);
        }
    }

    private void sendMessage(String message, Session session) {
        session.getRemote().sendString(message,null);
    }

    private String getWsMessage(String topic, String message) throws JsonProcessingException {
        WebSocketMessage webSocketMessage = new WebSocketMessage(topic, message);
        return mapper.writeValueAsString(webSocketMessage);
    }

    public void shutdown() {
        topicSubscriptions.values().forEach(topicSessions -> topicSessions.forEach(Session::close));
    }
}
