package org.safehaus.subutai.core.peer.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CleanCommandTest
{
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    CleanCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new CleanCommand();
        command.setPeerManager( peerManager );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        verify( localPeer ).cleanDb();
    }
}
