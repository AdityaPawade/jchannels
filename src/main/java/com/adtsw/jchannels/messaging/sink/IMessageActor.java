package com.adtsw.jchannels.messaging.sink;

public interface IMessageActor<I, O> {

    void setResponder(String topic, AbstractMessageResponder<I, O> responder);

    O getResponse(String topic, I message);
}
