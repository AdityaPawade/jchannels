package com.adtsw.jchannels.rate.enforcer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CountBasedRateLimitEnforcerWithTTL extends CountBasedRateLimitEnforcer {

    private final int ttlInMillis;
    private List<Long> expiryEvents;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public CountBasedRateLimitEnforcerWithTTL(int threshold, int ttlInSeconds) {
        super(threshold);
        this.ttlInMillis = ttlInSeconds * 1000;
        this.expiryEvents = new ArrayList<>();
    }

    public boolean increment() {
        writeLock.lock();
        removeExpiredCounts();
        writeLock.unlock();
        boolean allowed = super.increment();
        if(allowed) {
            writeLock.lock();
            long expiryTime = System.currentTimeMillis() + ttlInMillis;
            expiryEvents.add(expiryTime);
            writeLock.unlock();
        }
        return allowed;
    }

    public void decrement() {
        super.decrement();
    }

    @Override
    public int getRate() {
        return super.getRate();
    }

    public boolean isThresholdCrossed() {
        return super.isThresholdCrossed();
    }
    
    private void removeExpiredCounts() {
        boolean reachedUnexpiredEvent = false;
        long currentTimeMillis = System.currentTimeMillis();
        int expiredCount = 0;
        int expiryEventSize = expiryEvents.size();
        while(!reachedUnexpiredEvent && expiredCount < expiryEventSize) {
            if(currentTimeMillis > expiryEvents.get(expiredCount)) {
                expiredCount = expiredCount + 1;
                decrement();
            } else {
                reachedUnexpiredEvent = true;
            }
        }
        expiryEvents = expiryEvents.subList(expiredCount, expiryEventSize);
    }
}
