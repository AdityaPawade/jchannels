package com.adtsw.jchannels.server.ws;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class LogSocket implements WebSocketListener
{
    private boolean verbose = false;

    public boolean isVerbose()
    {
        return verbose;
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        if (verbose)
        {
            System.err.printf("onWebSocketBinary(byte[%d] payload, %d, %d)%n",payload.length,offset,len);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        if (verbose)
        {
            System.err.printf("onWebSocketClose(%d, %s)%n",statusCode,quote(reason));
        }
    }

    @Override
    public void onWebSocketConnect(Session session)
    {
        if (verbose)
        {
            System.err.printf("onWebSocketConnect(%s)%n",session);
        }
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        if (verbose)
        {
            System.err.printf("onWebSocketError((%s) %s)%n",cause.getClass().getName(),cause.getMessage());
            cause.printStackTrace(System.err);
        }
    }

    @Override
    public void onWebSocketText(String message)
    {
        if (verbose)
        {
            System.err.printf("onWebSocketText(%s)%n",quote(message));
        }
    }

    private String quote(String str)
    {
        if (str == null)
        {
            return "<null>";
        }
        return '"' + str + '"';
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

}
