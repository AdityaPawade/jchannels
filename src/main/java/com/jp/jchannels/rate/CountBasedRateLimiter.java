package com.jp.jchannels.rate;

import com.jp.jchannels.rate.enforcer.CountBasedRateLimitEnforcer;

public class CountBasedRateLimiter extends AbstractRateLimiter {

    private final int threshold;

    public CountBasedRateLimiter(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public IRateLimitEnforcer createEnforcer() {
        return new CountBasedRateLimitEnforcer(threshold);
    }
}
