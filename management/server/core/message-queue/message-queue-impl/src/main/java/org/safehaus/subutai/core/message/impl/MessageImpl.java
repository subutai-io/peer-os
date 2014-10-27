package org.safehaus.subutai.core.message.impl;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.message.api.Message;
import org.safehaus.subutai.core.message.api.MessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Implementation of Message
 */
public class MessageImpl implements Message
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageImpl.class.getName() );
    private String sender;
    private String payloadString;


    public MessageImpl( Object payload ) throws MessageException
    {
        Preconditions.checkNotNull( payload, "Payload is null" );

        payloadString = JsonUtil.toJson( payload );
    }


    @Override
    public <T> T getPayload( Class<T> clazz ) throws MessageException
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sender ), "Invalid sender" );

        this.sender = sender;
    }
}
