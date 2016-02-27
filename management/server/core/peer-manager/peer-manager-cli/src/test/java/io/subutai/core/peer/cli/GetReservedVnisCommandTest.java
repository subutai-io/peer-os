package io.subutai.core.peer.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.network.Vnis;
import io.subutai.common.peer.Peer;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GetReservedVnisCommandTest
{
    @Mock
    PeerManager peerManager;
    @Mock
    Peer peer;
    @Mock
    Vnis vnis;

    GetReservedVnisCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new GetReservedVnisCommand();
        command.setPeerManager( peerManager );

        when( peer.getReservedVnis() ).thenReturn( vnis );

        when( peerManager.getPeer( anyString() ) ).thenReturn( peer );
    }


    @Test
    public void testDoExecute() throws Exception
    {

        command.doExecute();

        verify( peer ).getReservedVnis();
    }
}
