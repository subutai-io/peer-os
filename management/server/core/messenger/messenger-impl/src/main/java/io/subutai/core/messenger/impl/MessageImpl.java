package io.subutai.core.messenger.impl;


import java.util.UUID;

import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.messenger.api.Message;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


/**
 * Implementation of Message
 */
public class MessageImpl implements Message
{
    public static final int MAX_SENDER_LEN = 50;
    private final UUID id;
    private final UUID sourcePeerId;
    private String sender;
    private String payloadString;


    public MessageImpl( Message message )
    {
        sourcePeerId = message.getSourcePeerId();
        payloadString = message.getPayload();
        id = message.getId();
        sender = message.getSender();
    }


    public MessageImpl( UUID sourcePeerId, Object payload )
    {
        Preconditions.checkNotNull( sourcePeerId, "Source peer id is null" );
        Preconditions.checkNotNull( payload, "Payload is null" );

        this.sourcePeerId = sourcePeerId;
        payloadString = JsonUtil.toJson( payload );
        id = UUID.randomUUID();
    }


    @Override
    public UUID getSourcePeerId()
    {
        return sourcePeerId;
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


    @Override
    public String getPayload()
    {
        return payloadString;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "id", id ).add( "sourcePeerId", sourcePeerId )
                      .add( "sender", sender ).add( "payloadString", payloadString ).toString();
    }
}
