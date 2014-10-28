package org.safehaus.subutai.core.message.impl;


import java.util.UUID;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.message.api.Message;

import com.google.common.base.Preconditions;


/**
 * Implementation of Message
 */
public class MessageImpl implements Message
{
    private static final int MAX_SENDER_LEN = 50;
    private final UUID id;
    private String sender;
    private String payloadString;


    public MessageImpl( Object payload )
    {
        Preconditions.checkNotNull( payload, "Payload is null" );

        payloadString = JsonUtil.toJson( payload );

        id = UUID.randomUUID();
    }


    @Override
    public UUID getId()
    {
        return id;
    }


    @Override
    public <T> T getPayload( Class<T> clazz )
    {
        return JsonUtil.fromJson( payloadString, clazz );
    }


    @Override
    public String getSender()
    {
        return sender;
    }


    @Override
    public void setSender( final String sender )
    {
        Preconditions.checkArgument( StringUtil.getLen( sender ) <= MAX_SENDER_LEN,
                String.format( "Max sender length must be %d", MAX_SENDER_LEN ) );

        this.sender = sender;
    }
}
