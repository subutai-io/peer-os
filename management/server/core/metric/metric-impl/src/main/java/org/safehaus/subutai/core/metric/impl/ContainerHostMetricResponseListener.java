package org.safehaus.subutai.core.metric.impl;


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
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContainerHostMetricResponseListener extends MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerHostMetricResponseListener.class.getName() );
    private Map<UUID, Semaphore> semaphoreMap = new ConcurrentHashMap<>();
    private ExpiringCache<UUID, Set<ContainerHostMetric>> containers = new ExpiringCache<>();


    protected ContainerHostMetricResponseListener()
    {
        super( RecipientType.METRIC_RESPONSE_RECIPIENT.name() );
    }


    public Set<ContainerHostMetric> waitMetrics( UUID requestId )
    {
        Semaphore completionSemaphore = new Semaphore( 0 );
        //put semaphore to map so that response can release it
        semaphoreMap.put( requestId, completionSemaphore );
        //wait for metrics
        try
        {
            completionSemaphore.tryAcquire( Constants.METRIC_REQUEST_TIMEOUT * 2 + 5, TimeUnit.SECONDS );
        }
        catch ( InterruptedException e )
        {
            LOG.warn( "ignore", e );
        }

        //remove semaphore from map
        semaphoreMap.remove( requestId );
        //obtain containers
        Set<ContainerHostMetric> metrics = containers.remove( requestId );
        //return
        return metrics == null ? Collections.<ContainerHostMetric>emptySet() : metrics;
    }


    @Override
    public void onMessage( final Message message )
    {
        ContainerHostMetricResponse response = message.getPayload( ContainerHostMetricResponse.class );
        //store containers to map for waiting thread
        containers.put( response.getRequestId(), response.getMetrics(), 5 * 1000 );

        //obtain semaphore from map
        Semaphore semaphore = semaphoreMap.remove( response.getRequestId() );

        //release semaphore to unblock waiting thread
        if ( semaphore != null )
        {
            semaphore.release();
        }
    }
}
