package io.subutai.core.peer.impl.request;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.Disposable;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageListener;
import io.subutai.core.messenger.api.MessageStatus;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.impl.RecipientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageResponseListener extends MessageListener implements Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageResponseListener.class.getName() );

    private Messenger messenger;
    protected Map<UUID, Semaphore> semaphoreMap = new ConcurrentHashMap<>();
    protected ExpiringCache<UUID, MessageResponse> responses = new ExpiringCache<>();


    public MessageResponseListener( Messenger messenger )
    {
        super( RecipientType.PEER_RESPONSE_LISTENER.name() );

        this.messenger = messenger;
    }


    public void dispose()
    {
        responses.dispose();
    }


    public MessageResponse waitResponse( MessageRequest request, int requestTimeout, int responseTimeout )
            throws PeerException
    {

        MessageStatus messageStatus;
        try
        {
            long start = System.currentTimeMillis();

            messageStatus = messenger.getMessageStatus( request.getMessageId() );

            while ( messageStatus == MessageStatus.IN_PROCESS
                    && System.currentTimeMillis() - start < ( requestTimeout + 5 ) * 1000 )
            {
                Thread.sleep( 100 );
                messageStatus = messenger.getMessageStatus( request.getMessageId() );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( e );
        }

        if ( messageStatus != MessageStatus.SENT )
        {
            throw new PeerException( "Failed to send message" );
        }


        //put semaphore to map so that response can release it
        semaphoreMap.put( request.getId(), new Semaphore( 0 ) );

        //wait for response
        try
        {
            semaphoreMap.get( request.getId() ).tryAcquire( responseTimeout + 5, TimeUnit.SECONDS );
        }
        catch ( InterruptedException e )
        {
            LOG.warn( "ignore", e );
        }

        //remove semaphore from map
        semaphoreMap.remove( request.getId() );

        //return responses
        return responses.remove( request.getId() );
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
