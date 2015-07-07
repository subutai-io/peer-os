package io.subutai.core.peer.impl.container;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.CreateContainerGroupRequest;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.Payload;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CreateContainerGroupRequestListenerTest
{
    @Mock
    LocalPeer localPeer;
    @Mock
    Payload payload;
    @Mock
    CreateContainerGroupRequest request;


    CreateContainerGroupRequestListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new CreateContainerGroupRequestListener( localPeer );
    }


    @Test
    public void testOnRequest() throws Exception
    {
        when( payload.getMessage( CreateContainerGroupRequest.class ) ).thenReturn( request );

        listener.onRequest( payload );

        verify( localPeer ).createContainerGroup( request );

        when( payload.getMessage( CreateContainerGroupRequest.class ) ).thenReturn( null );

        listener.onRequest( payload );

        verify( localPeer ).createContainerGroup( request );
    }
}
