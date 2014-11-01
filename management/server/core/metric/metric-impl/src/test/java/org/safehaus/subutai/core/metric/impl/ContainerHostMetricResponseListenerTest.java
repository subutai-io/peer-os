package org.safehaus.subutai.core.metric.impl;


import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostMetricResponseListenerTest
{
    ContainerHostMetricResponseListener listener;
    private static final UUID REQUEST_ID = UUID.randomUUID();
    @Spy
    Map<UUID, Semaphore> semaphoreMap = new ConcurrentHashMap();
    @Mock
    ExpiringCache<UUID, Set<ContainerHostMetric>> containers;


    @Before
    public void setUp() throws Exception
    {
        listener = new ContainerHostMetricResponseListener();
        listener.semaphoreMap = semaphoreMap;
        listener.containers = containers;
    }


    @Test
    public void testWaitMetrics() throws Exception
    {

        Thread t = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep( 1000 );
                }
                catch ( InterruptedException e )
                {

                }
                listener.semaphoreMap.get( REQUEST_ID ).release();
            }
        } );
        t.start();

        listener.waitMetrics( REQUEST_ID );

        verify( semaphoreMap ).remove( REQUEST_ID );

        Semaphore semaphore = mock( Semaphore.class );
        when( semaphoreMap.get( REQUEST_ID ) ).thenReturn( semaphore );
        InterruptedException exception = mock( InterruptedException.class );
        doThrow( exception ).when( semaphore ).tryAcquire( anyLong(), any( TimeUnit.class ) );


        listener.waitMetrics( REQUEST_ID );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testOnMessage() throws Exception
    {
        Message message = mock( Message.class );
        ContainerHostMetricResponse response = mock( ContainerHostMetricResponse.class );
        when( message.getPayload( ContainerHostMetricResponse.class ) ).thenReturn( response );
        when( response.getRequestId() ).thenReturn( REQUEST_ID );
        Semaphore semaphore = mock( Semaphore.class );
        when( semaphoreMap.remove( REQUEST_ID ) ).thenReturn( semaphore );
        when( semaphoreMap.get( REQUEST_ID ) ).thenReturn( semaphore );

        listener.onMessage( message );

        verify( containers ).put( eq( REQUEST_ID ), anySet(), anyInt() );
        verify( semaphore ).release();

    }
}
