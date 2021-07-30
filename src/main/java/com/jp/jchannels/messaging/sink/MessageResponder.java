package com.jp.jchannels.messaging.sink;

public abstract class MessageResponder<I, O> {

    public abstract O onMessage(I message);
}
