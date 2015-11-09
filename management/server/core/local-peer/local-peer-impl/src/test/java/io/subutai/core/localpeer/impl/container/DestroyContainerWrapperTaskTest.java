package io.subutai.core.localpeer.impl.container;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class DestroyContainerWrapperTaskTest
{
    @Mock
    LocalPeer localPeer;
    @Mock
    ContainerHost containerHost;

    DestroyContainerWrapperTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new DestroyContainerWrapperTask( localPeer, containerHost );
    }


    @Test
    public void testCall() throws Exception
    {
        task.call();

        verify( localPeer ).destroyContainer( containerHost.getContainerId() );
    }
}
