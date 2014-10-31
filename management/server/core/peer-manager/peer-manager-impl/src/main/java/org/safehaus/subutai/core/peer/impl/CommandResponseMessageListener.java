package org.safehaus.subutai.core.peer.impl;


import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.cache.EntryExpiryCallback;
import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageListener;


public class CommandResponseMessageListener extends MessageListener
{

    private ExpiringCache<UUID, CommandCallback> callbacks;


    public CommandResponseMessageListener()
    {
        super( RecipientType.COMMAND_RESPONSE.name() );
        callbacks = new ExpiringCache<>();
    }


    public void addCallback( UUID commandId, CommandCallback callback, int timeout, final Semaphore semaphore )
    {
        if ( callback != null )
        {
            callbacks.put( commandId, callback, timeout * 1000 + Constants.COMMAND_REQUEST_MESSAGE_TIMEOUT * 2 * 1000,
                    new EntryExpiryCallback<CommandCallback>()
                    {
                        @Override
                        public void onEntryExpiry( final CommandCallback entry )
                        {
                            if ( semaphore != null )
                            {
                                semaphore.release();
                            }
                        }
                    } );
        }
    }


    @Override
    public void onMessage( final Message message )
    {
        final CommandResponse commandResponse = message.getPayload( CommandResponse.class );

        UUID commandId = commandResponse.getCommandResult().getCommandId();
        CommandCallback callback = callbacks.get( commandId );

        if ( callback != null )
        {
            callback.onResponse( commandResponse.getResponse(), commandResponse.getCommandResult() );
        }
    }
}
