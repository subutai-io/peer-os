package org.safehaus.subutai.core.peer.impl;


import java.util.UUID;


public class MessageRequest<T>
{
    private UUID id;
    private T payload;
    private String recipient;


    public MessageRequest( final T payload, final String recipient )
    {
        this.id = UUID.randomUUID();
        this.payload = payload;
        this.recipient = recipient;
    }


    public UUID getId()
    {
        return id;
    }


    public T getPayload()
    {
        return payload;
    }


    public String getRecipient()
    {
        return recipient;
    }
}
