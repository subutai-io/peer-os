package org.safehaus.subutai.core.message.impl;


import java.util.UUID;

import org.safehaus.subutai.core.message.api.Message;
import org.safehaus.subutai.core.message.api.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Notifies listener on message
 */
public class MessageNotifier implements Runnable
{
    protected Logger LOG = LoggerFactory.getLogger( MessageNotifier.class.getName() );

    protected MessageListener listener;
    protected UUID sourcePeerId;
    protected Message message;


    public MessageNotifier( final MessageListener listener, final Message message, final UUID sourcePeerId )
    {
        Preconditions.checkNotNull( message, "Message is null" );
        Preconditions.checkNotNull( listener, "Listener is null" );
        Preconditions.checkNotNull( sourcePeerId, "Source peer id is null" );

        this.listener = listener;
        this.message = message;
        this.sourcePeerId = sourcePeerId;
    }


    @Override
    public void run()
    {
        try
        {
            listener.onMessage( sourcePeerId, message );
        }
        catch ( Exception e )
        {
            LOG.error(
                    String.format( "Error notifying %s on %s from %s", listener.getRecipient(), message, sourcePeerId ),
                    e );
        }
    }
}
