package io.subutai.core.peer.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.peer.Peer;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessageCommandTest
{
    private static final String PEER_ID = UUID.randomUUID().toString();
    @Mock
    PeerManager peerManager;
    @Mock
    Peer peer;

    MessageCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new MessageCommand( peerManager );
        command.peerId = PEER_ID.toString();
        when( peerManager.getPeer( PEER_ID ) ).thenReturn( peer );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        verify( peer ).sendRequest( anyString(), anyString(), anyInt(), any( Class.class ), anyInt(), anyMap() );
    }
}
