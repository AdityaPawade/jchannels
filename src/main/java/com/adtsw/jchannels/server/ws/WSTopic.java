package com.adtsw.jchannels.server.ws;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class WSTopic {

    @Getter
    private final String name;
    private final boolean throttle;
    @Getter
    private final int minMessageInterval;

    public boolean shouldThrottle() {
        return throttle;
    }

    public WSTopic(String name) {
        this.name = name;
        this.throttle = false;
        this.minMessageInterval = 0;
    }
}
