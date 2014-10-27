package org.safehaus.subutai.core.message.impl;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
    private byte[] payloadBytes;


    public MessageImpl( Serializable payload ) throws MessageException
    {
        Preconditions.checkNotNull( payload, "Payload is null" );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try
        {
            out = new ObjectOutputStream( bos );
            out.writeObject( payload );
            payloadBytes = bos.toByteArray();
        }
        catch ( IOException e )
        {
            LOG.error( "Error in constructor", e );
            throw new MessageException( e );
        }
        finally
        {
            try
            {
                if ( out != null )
                {
                    out.close();
                }
            }
            catch ( IOException ex )
            {
                // ignore close exception
            }
            try
            {
                bos.close();
            }
            catch ( IOException ex )
            {
                // ignore close exception
            }
        }
    }


    @Override
    public Object getPayload() throws MessageException
    {
        ByteArrayInputStream bis = new ByteArrayInputStream( payloadBytes );
        ObjectInput in = null;
        try
        {
            in = new ObjectInputStream( bis );
            return in.readObject();
        }
        catch ( IOException | ClassNotFoundException e )
        {
            LOG.error( "Error in getPayload", e );
            throw new MessageException( e );
        }
        finally
        {
            try
            {
                bis.close();
            }
            catch ( IOException ex )
            {
                // ignore close exception
            }
            try
            {
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( IOException ex )
            {
                // ignore close exception
            }
        }
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
