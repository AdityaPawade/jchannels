package com.adtsw.jchannels.rate;

import com.adtsw.jchannels.rate.enforcer.CountBasedRateLimitEnforcer;

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
