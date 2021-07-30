package com.jp.jchannels.rate;

public interface IRateLimitEnforcer {

    boolean increment();
    
    void decrement();
    
    int getRate();

    boolean isThresholdCrossed();
}
