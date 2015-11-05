package io.subutai.core.peer.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.ContainerHost;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class StartLxcCommandTest
{
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    ContainerHost containerHost;

    StartLxcCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new StartLxcCommand();
        command.setPeerManager( peerManager );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getContainerHostByName( anyString() ) ).thenReturn( containerHost );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        verify( localPeer ).startContainer( containerHost.getContainerId() );
    }
}
