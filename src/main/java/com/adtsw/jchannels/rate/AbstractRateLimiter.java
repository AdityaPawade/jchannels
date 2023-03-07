package com.adtsw.jchannels.rate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRateLimiter implements IRateLimiter {

    private static final String defaultKey = "default";
    private final Map<String, IRateLimitEnforcer> rate = new ConcurrentHashMap<>();

    @Override
    public boolean allow() {
        return allow(defaultKey);
    }

    public boolean allow(String key) {
        IRateLimitEnforcer rateLimit = rate.compute(key, (existingKey, existingValue) -> {
            if (existingValue == null) {
                IRateLimitEnforcer newEnforcer = createEnforcer();
                newEnforcer.increment();
                return newEnforcer;
            } else {
                existingValue.increment();
                return existingValue;
            }
        });
        return !rateLimit.isThresholdCrossed();
    }

    @Override
    public void exit() {
        exit(defaultKey);
    }

    public void exit(String key) {
        rate.compute(key, (existingKey, existingValue) -> {
            if(existingValue == null) {
                return createEnforcer();
            } else {
                existingValue.decrement();
                return existingValue;
            }
        });
    }

    @Override
    public int getRate() {
        return getRate(defaultKey);
    }

    @Override
    public int getRate(String key) {
        IRateLimitEnforcer enforcer = rate.get(key);
        if(enforcer != null) return enforcer.getRate();
        else return 0;
    }

    public abstract IRateLimitEnforcer createEnforcer();
}
