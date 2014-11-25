package org.safehaus.subutai.core.metric.impl;


import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.peer.api.Payload;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RemoteMetricRequestListenerTest
{
    @Mock
    MonitorImpl monitor;
    @Mock
    Payload payload;
    @Mock
    ContainerHostMetricRequest request;
    @Mock
    Set<ContainerHostMetricImpl> metrics;

    RemoteMetricRequestListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new RemoteMetricRequestListener( monitor );
    }


    @Test
    public void testOnRequest() throws Exception
    {
        when( payload.getMessage( ContainerHostMetricRequest.class ) ).thenReturn( request );

        when( monitor.getLocalContainerHostsMetrics( any( UUID.class ) ) ).thenReturn( metrics );

        when( metrics.isEmpty() ).thenReturn( false );

        ContainerHostMetricResponse response = ( ContainerHostMetricResponse ) listener.onRequest( payload );

        assertEquals( metrics, response.getMetrics() );

        when( metrics.isEmpty() ).thenReturn( true );
        reset( request );

        listener.onRequest( payload );

        verify( request, times( 2 ) ).getEnvironmentId();

        reset( payload );

        listener.onRequest( payload );
    }
}
