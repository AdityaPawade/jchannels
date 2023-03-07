package com.adtsw.jchannels.server.ws;

import org.eclipse.jetty.websocket.api.Session;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KeepAlive extends Thread {

    private CountDownLatch latch;
    private Session session;

    public KeepAlive(Session session)
    {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            while (!latch.await(10, TimeUnit.SECONDS)) {
                System.err.println("Ping");
                ByteBuffer data = ByteBuffer.allocate(3);
                data.put(new byte[]
                    { (byte)1, (byte)2, (byte)3 });
                data.flip();
                session.getRemote().sendPing(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public synchronized void start() {
        latch = new CountDownLatch(1);
        super.start();
    }
}