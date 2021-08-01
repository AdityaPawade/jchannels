package com.adtsw.jchannels.messaging.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BroadcastMessage<I> {

    private final I message;
    private final boolean sticky;
}
