package io.subutai.core.peer.impl.request;


import java.util.Map;
import java.util.UUID;

import io.subutai.core.peer.api.Payload;


public class MessageRequest
{
    private UUID id;
    private Payload payload;
    private String recipient;
    private Map<String, String> headers;
    private UUID messageId;


    public MessageRequest( final Payload payload, final String recipient, final Map<String, String> headers )
    {
        this.id = UUID.randomUUID();
        this.payload = payload;
        this.recipient = recipient;
        this.headers = headers;
    }


    public UUID getMessageId()
    {
        return messageId;
    }


    public void setMessageId( final UUID messageId )
    {
        this.messageId = messageId;
    }


    public UUID getId()
    {
        return id;
    }


    public Payload getPayload()
    {
        return payload;
    }


    public String getRecipient()
    {
        return recipient;
    }


    public Map<String, String> getHeaders()
    {
        return headers;
    }
}
