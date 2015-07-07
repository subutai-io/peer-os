package io.subutai.core.messenger.impl;


import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Notifies listener on message
 */
public class MessageNotifier implements Runnable
{
    private static Logger LOG = LoggerFactory.getLogger( MessageNotifier.class.getName() );

    protected MessageListener listener;
    protected Message message;


    public MessageNotifier( final MessageListener listener, final Message message )
    {
        Preconditions.checkNotNull( message, "Message is null" );
        Preconditions.checkNotNull( listener, "Listener is null" );

        this.listener = listener;
        this.message = message;
    }


    @Override
    public void run()
    {
        try
        {
            listener.onMessage( message );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error notifying %s on %s", listener.getRecipient(), message ), e );
        }
    }
}
