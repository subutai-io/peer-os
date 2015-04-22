package org.safehaus.subutai.core.peer.impl.container;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.ContainersDestructionResult;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Payload;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class DestroyEnvironmentContainersRequestListenerTest
{
    @Mock
    LocalPeer localPeer;
    @Mock
    Payload payload;
    @Mock
    ContainersDestructionResult result;
    @Mock
    DestroyEnvironmentContainersRequest request;


    DestroyEnvironmentContainersRequestListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new DestroyEnvironmentContainersRequestListener( localPeer );
    }


    @Test
    public void testOnRequest() throws Exception
    {
        when( payload.getMessage( DestroyEnvironmentContainersRequest.class ) ).thenReturn( request );
        when( localPeer.destroyEnvironmentContainers( any( UUID.class ) ) ).thenReturn( result );

        listener.onRequest( payload );

        verify( localPeer ).destroyEnvironmentContainers( any( UUID.class ) );


        when( payload.getMessage( DestroyEnvironmentContainersRequest.class ) ).thenReturn( null );

        verify( localPeer ).destroyEnvironmentContainers( any( UUID.class ) );

    }
}
