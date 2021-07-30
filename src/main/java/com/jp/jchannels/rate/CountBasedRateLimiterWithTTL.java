package com.jp.jchannels.rate;

import com.jp.jchannels.rate.enforcer.CountBasedRateLimitEnforcerWithTTL;

public class CountBasedRateLimiterWithTTL extends AbstractRateLimiter {

    private final int threshold;
    private final int ttlInSeconds;

    public CountBasedRateLimiterWithTTL(int threshold, int ttlInSeconds) {
        this.threshold = threshold;
        this.ttlInSeconds = ttlInSeconds;
    }

    @Override
    public IRateLimitEnforcer createEnforcer() {
        return new CountBasedRateLimitEnforcerWithTTL(threshold, ttlInSeconds);
    }
}
