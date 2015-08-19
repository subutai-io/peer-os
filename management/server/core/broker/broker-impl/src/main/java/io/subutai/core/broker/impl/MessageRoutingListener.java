package io.subutai.core.broker.impl;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.broker.api.ByteMessageInterceptor;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.TextMessageInterceptor;
import io.subutai.core.broker.api.TextMessageListener;


/**
 * Routes message to listeners
 */
public class MessageRoutingListener implements MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageRoutingListener.class.getName() );

    protected Set<io.subutai.core.broker.api.MessageListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<io.subutai.core.broker.api.MessageListener, Boolean>() );
    protected ByteMessageInterceptor byteMessageInterceptor;
    protected TextMessageInterceptor textMessageInterceptor;


    public void addListener( io.subutai.core.broker.api.MessageListener listener )
    {
        listeners.add( listener );
    }


    public void setByteMessagePreProcessor( ByteMessageInterceptor byteMessagePreProcessor )
    {
        this.byteMessageInterceptor = byteMessagePreProcessor;
    }


    public void setTextMessagePreProcessor( TextMessageInterceptor textMessagePreProcessor )
    {
        this.textMessageInterceptor = textMessagePreProcessor;
    }


    public void removeListener( io.subutai.core.broker.api.MessageListener listener )
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

            for ( io.subutai.core.broker.api.MessageListener listener : listeners )
            {
                if ( listener.getTopic().name().equalsIgnoreCase( destination.getTopicName() ) )
                {
                    notifyListener( listener, message, listener.getTopic() );
                }
            }
        }
        catch ( JMSException e )
        {
            LOG.error( "Error in onMessage", e );
        }
    }


    protected void notifyListener( io.subutai.core.broker.api.MessageListener listener, Message message,
                                   io.subutai.core.broker.api.Topic topic )
    {
        try
        {
            if ( message instanceof BytesMessage && listener instanceof ByteMessageListener )
            {
                BytesMessage msg = ( BytesMessage ) message;
                byte[] bytes = new byte[( int ) msg.getBodyLength()];
                msg.readBytes( bytes );

                //pre-process the message
                if ( byteMessageInterceptor != null )
                {
                    bytes = byteMessageInterceptor.process( topic.name(), bytes );
                }

                ( ( ByteMessageListener ) listener ).onMessage( bytes );
            }
            else if ( message instanceof TextMessage && listener instanceof TextMessageListener )
            {
                TextMessage msg = ( TextMessage ) message;
                String msgTxt = msg.getText();

                //pre-process the message
                if ( textMessageInterceptor != null )
                {
                    msgTxt = textMessageInterceptor.process( topic.name(), msgTxt );
                }

                ( ( TextMessageListener ) listener ).onMessage( msgTxt );
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
