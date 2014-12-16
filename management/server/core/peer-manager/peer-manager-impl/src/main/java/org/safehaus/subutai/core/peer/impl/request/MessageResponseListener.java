package org.safehaus.subutai.core.peer.impl.request;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.peer.impl.RecipientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageResponseListener extends MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageResponseListener.class.getName() );

    private Map<UUID, Semaphore> semaphoreMap = new ConcurrentHashMap<>();
    private ExpiringCache<UUID, MessageResponse> responses = new ExpiringCache<>();


    public MessageResponseListener()
    {
        super( RecipientType.PEER_RESPONSE_LISTENER.name() );
    }


    public MessageResponse waitResponse( UUID requestId, int requestTimeout, int responseTimeout )
    {
        //put semaphore to map so that response can release it
        semaphoreMap.put( requestId, new Semaphore( 0 ) );

        //wait for response
        try
        {
            semaphoreMap.get( requestId ).tryAcquire( requestTimeout + responseTimeout + 5, TimeUnit.SECONDS );
        }
        catch ( InterruptedException e )
        {
            LOG.warn( "ignore", e );
        }

        //remove semaphore from map
        semaphoreMap.remove( requestId );

        //return responses
        return responses.remove( requestId );
    }


    @Override
    public void onMessage( final Message message )
    {
        MessageResponse messageResponse = message.getPayload( MessageResponse.class );
        //store response to map for waiting thread
        responses.put( messageResponse.getRequestId(), messageResponse, 5 * 1000 );

        //obtain semaphore from map
        Semaphore semaphore = semaphoreMap.remove( messageResponse.getRequestId() );

        //release semaphore to unblock waiting thread
        if ( semaphore != null )
        {
            semaphore.release();
        }
    }
}
