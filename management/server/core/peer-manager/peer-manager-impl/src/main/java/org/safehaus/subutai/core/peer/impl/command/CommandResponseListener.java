package org.safehaus.subutai.core.peer.impl.command;


import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.cache.EntryExpiryCallback;
import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.impl.RecipientType;
import org.safehaus.subutai.core.peer.impl.Timeouts;


public class CommandResponseListener extends RequestListener
{

    private ExpiringCache<UUID, CommandCallback> callbacks;


    public CommandResponseListener()
    {
        super( RecipientType.COMMAND_RESPONSE.name() );
        callbacks = new ExpiringCache<>();
    }


    public void addCallback( UUID commandId, CommandCallback callback, int timeout, final Semaphore semaphore )
    {
        if ( callback != null )
        {
            callbacks.put( commandId, callback, timeout * 1000 + Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT * 2 * 1000,
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
    public Object onRequest( final Payload payload )
    {
        final CommandResponse commandResponse = payload.getMessage( CommandResponse.class );

        CommandCallback callback = callbacks.get( commandResponse.getRequestId() );

        if ( callback != null )
        {
            callback.onResponse( commandResponse.getResponse(), commandResponse.getCommandResult() );
        }

        return null;
    }
}
