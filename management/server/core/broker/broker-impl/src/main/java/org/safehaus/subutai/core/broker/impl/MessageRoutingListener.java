package org.safehaus.subutai.core.broker.impl;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.TextMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Routes message to listeners
 */
public class MessageRoutingListener implements MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageRoutingListener.class.getName() );

    protected Set<org.safehaus.subutai.core.broker.api.MessageListener> listeners = Collections
            .newSetFromMap( new ConcurrentHashMap<org.safehaus.subutai.core.broker.api.MessageListener, Boolean>() );


    public void addListener( org.safehaus.subutai.core.broker.api.MessageListener listener )
    {
        listeners.add( listener );
    }


    public void removeListener( org.safehaus.subutai.core.broker.api.MessageListener listener )
    {
        listeners.remove( listener );
    }


    @Override
    public void onMessage( final Message message )
    {
        try
        {
            //ack message
            message.acknowledge();

            //assume we are always using topics
            Topic destination = ( Topic ) message.getJMSDestination();

            for ( org.safehaus.subutai.core.broker.api.MessageListener listener : listeners )
            {
                if ( listener.getTopic().name().equalsIgnoreCase( destination.getTopicName() ) )
                {
                    notifyListener( listener, message );
                }
            }
        }
        catch ( JMSException e )
        {
            LOG.error( "Error in onMessage", e );
        }
    }


    protected void notifyListener( org.safehaus.subutai.core.broker.api.MessageListener listener, Message message )
    {
        try
        {
            if ( message instanceof BytesMessage && listener instanceof ByteMessageListener )
            {
                BytesMessage msg = ( BytesMessage ) message;
                byte[] bytes = new byte[( int ) msg.getBodyLength()];
                msg.readBytes( bytes );
                ( ( ByteMessageListener ) listener ).onMessage( bytes );
            }
            else if ( message instanceof TextMessage && listener instanceof TextMessageListener )
            {
                TextMessage msg = ( TextMessage ) message;
                ( ( TextMessageListener ) listener ).onMessage( msg.getText() );
            }
            else
            {
                LOG.warn( String.format( "Message type %s and listener type %s didn't match", message, listener ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in notifyListener", e );
        }
    }
}
