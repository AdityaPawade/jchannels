package com.adtsw.jchannels.messaging.queue;

public abstract class MessageListener<I> {

    public abstract void onMessage(I message);
}
