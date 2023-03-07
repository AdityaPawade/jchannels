package com.adtsw.jchannels.model;

public class WebSocketMessage {

    private String topic;
    private String message;

    public WebSocketMessage(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    public WebSocketMessage() {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}