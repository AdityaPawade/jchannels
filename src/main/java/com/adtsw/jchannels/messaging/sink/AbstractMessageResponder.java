package com.adtsw.jchannels.messaging.sink;

public abstract class AbstractMessageResponder<I, O> {

    public abstract O onMessage(I message);
}
