package org.safehaus.subutai.core.peer.impl;


import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateContainerResponseListener extends MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CreateContainerResponseListener.class.getName() );


    private Map<UUID, Semaphore> semaphoreMap = new ConcurrentHashMap<>();
    private ExpiringCache<UUID, Set<ContainerHost>> containers = new ExpiringCache<>();


    protected CreateContainerResponseListener()
    {
        super( RecipientType.CONTAINER_CREATE_RESPONSE.name() );
    }


    public Set<ContainerHost> waitContainers( UUID requestId )
    {
        Semaphore completionSemaphore = new Semaphore( 0 );
        //put semaphore to map so that response can release it
        semaphoreMap.put( requestId, completionSemaphore );
        //wait for containers
        try
        {
            completionSemaphore.tryAcquire(
                    Timeouts.CREATE_CONTAINER_REQUEST_TIMEOUT + Timeouts.CREATE_CONTAINER_RESPONSE_TIMEOUT + 5,
                    TimeUnit.SECONDS );
        }
        catch ( InterruptedException e )
        {
            LOG.warn( "ignore", e );
        }

        //remove semaphore from map
        semaphoreMap.remove( requestId );
        //obtain containers
        Set<ContainerHost> containerHosts = containers.remove( requestId );
        //return
        return containerHosts == null ? Collections.<ContainerHost>emptySet() : containerHosts;
    }


    @Override
    public void onMessage( final Message message )
    {
        CreateContainerResponse response = message.getPayload( CreateContainerResponse.class );
        //store containers to map for waiting thread
        containers.put( response.getRequestId(), response.getContainerHosts(), 5 * 1000 );

        //obtain semaphore from map
        Semaphore semaphore = semaphoreMap.remove( response.getRequestId() );

        //release semaphore to unblock waiting thread
        if ( semaphore != null )
        {
            semaphore.release();
        }
    }
}
