package com.adtsw.jchannels.server.ws;

import org.eclipse.jetty.websocket.api.Session;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSocket {

    private final CountDownLatch closeLatch;
    private Session session;

    public AbstractSocket(int latchCountDown) {
        this.closeLatch = new CountDownLatch(latchCountDown);
    }

    public AbstractSocket() {
        this(0);
    }

    public Session getSession() {
        return session;
    }

    public void removeSession() {
        this.session = null;
    }

    public void setSession(Session session) {
        this.session = session;
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
