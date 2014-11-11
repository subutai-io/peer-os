package org.safehaus.subutai.core.broker.impl;


import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.MessageListener;
import org.safehaus.subutai.core.broker.api.TextMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Notifies listener on message
 */
public class MessageNotifier implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageNotifier.class.getName() );

    protected MessageListener listener;
    protected Message message;


    public MessageNotifier( final MessageListener listener, final Message message )
    {
        this.listener = listener;
        this.message = message;
    }


    @Override
    public void run()
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
            LOG.error( "Error in onMessage", e );
        }
    }
}
