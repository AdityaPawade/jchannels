package com.adtsw.jchannels.rate;

public interface IRateLimiter {

    boolean allow();
    boolean allow(String key);
    void exit();
    void exit(String key);
    int getRate();
    int getRate(String key);
}
