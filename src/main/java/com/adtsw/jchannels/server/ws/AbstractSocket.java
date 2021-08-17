package com.adtsw.jchannels.server.ws;

import org.eclipse.jetty.websocket.api.Session;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSocket {

    private final CountDownLatch closeLatch;
    private final long idleTimeoutMs;
    private Session session;

    public AbstractSocket(int latchCountDown, long idleTimeoutMs) {
        this.closeLatch = new CountDownLatch(latchCountDown);
        this.idleTimeoutMs = idleTimeoutMs;
    }

    public AbstractSocket() {
        this(0, 300000);
    }

    public Session getSession() {
        return session;
    }

    public void removeSession() {
        this.session = null;
    }

    public void setSession(Session session) {
        this.session = session;
        this.session.setIdleTimeout(idleTimeoutMs);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
//        return true;
    }

    public void awaitClose() throws InterruptedException {
        this.closeLatch.await();
    }
    
    protected void countDown() {
        // trigger latch
        this.closeLatch.countDown();
    }
    
    public boolean isOpen() {
        return session != null && session.isOpen();
    }
}
