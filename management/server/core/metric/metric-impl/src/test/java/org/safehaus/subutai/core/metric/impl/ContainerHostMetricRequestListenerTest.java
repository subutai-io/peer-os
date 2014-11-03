package org.safehaus.subutai.core.metric.impl;


import java.io.PrintStream;
import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostMetricRequestListenerTest
{

    private static final UUID ENVIRONMENT_ID = UUID.randomUUID();
    @Mock
    Messenger messenger;
    @Mock
    MonitorImpl monitor;
    @Mock
    PeerManager peerManager;

    ContainerHostMetricRequestListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new ContainerHostMetricRequestListener( monitor, messenger, peerManager );
    }


    @Test
    public void testOnMessage() throws Exception
    {
        Message message = mock( Message.class );
        ContainerHostMetricRequest request = mock( ContainerHostMetricRequest.class );
        when( request.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
        when( message.getPayload( ContainerHostMetricRequest.class ) ).thenReturn( request );
        ContainerHostMetricImpl metric = mock( ContainerHostMetricImpl.class );
        when( monitor.getLocalContainerHostMetrics( ENVIRONMENT_ID ) ).thenReturn( Sets.newHashSet( metric ) );

        listener.onMessage( message );

        verify( peerManager ).getPeer( any( UUID.class ) );
        verify( messenger ).createMessage( isA( ContainerHostMetricResponse.class ) );
        verify( messenger ).sendMessage( any( Peer.class ), any( Message.class ), anyString(), anyInt() );


        MessageException exception = mock( MessageException.class );
        doThrow( exception ).when( messenger )
                            .sendMessage( any( Peer.class ), any( Message.class ), anyString(), anyInt() );

        listener.onMessage( message );

        verify( exception ).printStackTrace( any( PrintStream.class ) );

        when( monitor.getLocalContainerHostMetrics( ENVIRONMENT_ID ) )
                .thenReturn( Collections.<ContainerHostMetricImpl>emptySet() );

        reset( request );

        listener.onMessage( message );

        verify( request, times( 2 ) ).getEnvironmentId();
    }
}
