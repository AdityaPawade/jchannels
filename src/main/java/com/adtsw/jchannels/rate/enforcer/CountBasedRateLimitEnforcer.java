package com.adtsw.jchannels.rate.enforcer;

import com.adtsw.jchannels.rate.IRateLimitEnforcer;

public class CountBasedRateLimitEnforcer implements IRateLimitEnforcer {

    private final int threshold;
    private int count = 0;
    private boolean thresholdCrossed = false;

    public CountBasedRateLimitEnforcer(int threshold) {
        this.threshold = threshold;
    }

    public boolean increment() {
        if(this.count > threshold - 1) {
            this.thresholdCrossed = true;
        } else {
            this.count = this.count + 1;
        }
        return !this.thresholdCrossed;
    }

    public void decrement() {
        if(this.count > 0) {
            this.count = this.count - 1;
        }
        if(this.count <= threshold) {
            this.thresholdCrossed = false;
        }
    }

    @Override
    public int getRate() {
        return this.count;
    }

    public boolean isThresholdCrossed() {
        return thresholdCrossed;
    }
}
