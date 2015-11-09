package io.subutai.core.peer.impl.command;


import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.cache.EntryExpiryCallback;
import io.subutai.common.cache.ExpiringCache;
import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandResponse;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.RecipientType;
import io.subutai.common.peer.RequestListener;
import io.subutai.common.peer.Timeouts;
import io.subutai.common.protocol.Disposable;


public class CommandResponseListener extends RequestListener implements Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandResponseListener.class.getName() );


    protected ExpiringCache<UUID, CommandCallback> callbacks;


    public CommandResponseListener()
    {
        super( RecipientType.COMMAND_RESPONSE.name() );
        callbacks = new ExpiringCache<>();
    }


    public void dispose()
    {
        callbacks.dispose();
    }


    public void addCallback( UUID commandId, CommandCallback callback, int timeout, final Semaphore semaphore )
    {
        if ( callback != null )
        {
            callbacks.put( commandId, callback, timeout * 1000 + Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT * 2 * 1000,
                    new CommandResponseExpiryCallback( semaphore ) );
        }
    }


    @Override
    public Object onRequest( final Payload payload )
    {
        final CommandResponse commandResponse = payload.getMessage( CommandResponse.class );

        if ( commandResponse != null )
        {

            CommandCallback callback = callbacks.get( commandResponse.getRequestId() );

            if ( callback != null )
            {
                callback.onResponse( commandResponse.getResponse(), commandResponse.getCommandResult() );
            }
        }
        else
        {
            LOG.warn( "Null response" );
        }
        return null;
    }


    protected static class CommandResponseExpiryCallback implements EntryExpiryCallback<CommandCallback>
    {

        private final Semaphore semaphore;


        public CommandResponseExpiryCallback( final Semaphore semaphore )
        {
            this.semaphore = semaphore;
        }


        @Override
        public void onEntryExpiry( final CommandCallback entry )
        {
            if ( semaphore != null )
            {
                semaphore.release();
            }
        }
    }
}
