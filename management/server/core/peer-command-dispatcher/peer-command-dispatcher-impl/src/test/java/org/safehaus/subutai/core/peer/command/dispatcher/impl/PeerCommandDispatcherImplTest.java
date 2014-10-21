package org.safehaus.subutai.core.peer.command.dispatcher.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 10/20/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class PeerCommandDispatcherImplTest
{
    PeerCommandDispatcherImpl commandDispatcher;
    @Mock
    PeerManager peerManager;
    @Mock
    RemotePeerRestClient client;


    @Before
    public void setUp() throws Exception
    {
        this.commandDispatcher = new PeerCommandDispatcherImpl();
        this.commandDispatcher.setPeerManager( peerManager );
        this.commandDispatcher.setRemotePeerRestClient( client );
    }


    @Test
    public void testInvoke() throws Exception
    {
        PeerCommandMessage message = mock( PeerCommandMessage.class );
        UUID uuid = UUID.randomUUID();
        message.setPeerId( uuid );
        Peer peer = mock( Peer.class );
        peer.setId( uuid );
        when( peerManager.getPeerByUUID( message.getPeerId() ) ).thenReturn( peer );
        commandDispatcher.invoke( message, 1000 );
    }


    @Test( expected = RuntimeException.class )
    public void testInvokeException()
    {
        PeerCommandMessage message = mock( PeerCommandMessage.class );
        UUID uuid = UUID.randomUUID();
        message.setPeerId( uuid );
        Peer peer = mock( Peer.class );
        peer.setId( uuid );
        when( peerManager.getPeerByUUID( message.getPeerId() ) ).thenReturn( peer );
        doThrow( new RuntimeException() ).when( client ).invoke( peer.getIp(), "8181", message );
        commandDispatcher.invoke( message, 1000 );
    }
}
