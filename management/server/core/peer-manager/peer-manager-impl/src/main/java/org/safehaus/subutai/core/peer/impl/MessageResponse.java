package org.safehaus.subutai.core.peer.impl;


import java.util.UUID;


public class MessageResponse
{
    private UUID requestId;
    private Object payload;
    private String exception;


    public MessageResponse( final UUID requestId, final Object payload, final String exception )
    {
        this.requestId = requestId;
        this.payload = payload;
        this.exception = exception;
    }


    public UUID getRequestId()
    {
        return requestId;
    }


    public Object getPayload()
    {
        return payload;
    }


    public String getException()
    {
        return exception;
    }
}
