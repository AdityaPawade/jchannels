package com.adtsw.jchannels.messaging.sink;

public interface MessageActor<I, O> {

    void setResponder(String topic, MessageResponder<I, O> responder);

    O getResponse(String topic, I message);
}
