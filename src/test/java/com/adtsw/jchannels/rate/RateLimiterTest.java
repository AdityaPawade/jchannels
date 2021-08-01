package com.adtsw.jchannels.rate;

import org.junit.Assert;
import org.junit.Test;

public class RateLimiterTest {

    @Test
    public void testCountBasedRateLimit() {

        String key1 = "key1";
        String key2 = "key2";
        IRateLimiter limiter = new CountBasedRateLimiter(2);
        Assert.assertEquals(limiter.allow(key1), true);
        Assert.assertEquals(limiter.allow(key2), true);
        Assert.assertEquals(limiter.allow(key2), true);
        Assert.assertEquals(limiter.allow(key1), true);
        Assert.assertEquals(limiter.allow(key1), false);
        limiter.exit(key1);
        Assert.assertEquals(limiter.allow(key1), true);
        Assert.assertEquals(limiter.allow(key2), false);
        Assert.assertEquals(limiter.allow(key1), false);
    }
    
    @Test
    public void testCountBasedRateLimitWithTTL() throws InterruptedException {

        String key1 = "key1";
        IRateLimiter limiter = new CountBasedRateLimiterWithTTL(2, 5);
        Assert.assertEquals(true, limiter.allow(key1));
        Assert.assertEquals(true, limiter.allow(key1));
        Assert.assertEquals(false, limiter.allow(key1));
        limiter.exit(key1);
        Assert.assertEquals(true, limiter.allow(key1));
        Assert.assertEquals(false, limiter.allow(key1));
        Thread.sleep(6 * 1000);
        Assert.assertEquals(true, limiter.allow(key1));
    }
}
