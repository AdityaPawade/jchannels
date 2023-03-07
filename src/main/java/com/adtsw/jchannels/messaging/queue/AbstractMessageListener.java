package com.adtsw.jchannels.messaging.queue;

public abstract class AbstractMessageListener<I> {

    public abstract void onMessage(I message);
}
