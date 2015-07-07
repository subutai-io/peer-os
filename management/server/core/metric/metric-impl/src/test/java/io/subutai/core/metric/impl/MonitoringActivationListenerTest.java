package io.subutai.core.metric.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.ContainerHost;
import io.subutai.core.metric.api.MonitoringSettings;
import io.subutai.core.metric.impl.MonitorImpl;
import io.subutai.core.metric.impl.MonitoringActivationListener;
import io.subutai.core.metric.impl.MonitoringActivationRequest;

import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MonitoringActivationListenerTest
{
    @Mock
    MonitorImpl monitor;
    @Mock
    PeerManager peerManager;
    @Mock
    Payload payload;
    @Mock
    MonitoringActivationRequest request;
    @Mock
    LocalPeer localPeer;

    MonitoringActivationListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new MonitoringActivationListener( monitor, peerManager );
    }


    @Test
    public void testOnRequest() throws Exception
    {
        when( payload.getMessage( MonitoringActivationRequest.class ) ).thenReturn( request );
        when( request.getContainerHostsIds() ).thenReturn( Sets.newHashSet( UUID.randomUUID() ) );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.bindHost( any( UUID.class ) ) ).thenReturn( mock( ContainerHost.class ) );

        listener.onRequest( payload );

        verify( monitor ).activateMonitoringAtLocalContainers( anySet(), any( MonitoringSettings.class ) );
    }
}
