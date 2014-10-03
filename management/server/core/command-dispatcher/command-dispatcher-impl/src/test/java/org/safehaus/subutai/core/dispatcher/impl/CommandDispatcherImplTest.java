package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for CommandDispatcherImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class CommandDispatcherImplTest
{
    @Mock
    AgentManager agentManager;
    @Mock
    CommandRunner commandRunner;
    @Mock
    DbManager dbManager;
    @Mock
    PeerManager peerManager;

    CommandDispatcherImpl commandDispatcher;
    private static final UUID agentId = UUID.randomUUID();
    private static final UUID commandId = UUID.randomUUID();
    private static final UUID peerId = UUID.randomUUID();
    private static final Integer timeout = 30;


    @Before
    public void setUp()
    {
        commandDispatcher = new CommandDispatcherImpl( agentManager, commandRunner, dbManager, peerManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new CommandDispatcherImpl( null, commandRunner, dbManager, peerManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullCommandRunner()
    {
        new CommandDispatcherImpl( agentManager, null, dbManager, peerManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullDbManager()
    {
        new CommandDispatcherImpl( agentManager, commandRunner, null, peerManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullPeerManager()
    {
        new CommandDispatcherImpl( agentManager, commandRunner, dbManager, null );
    }


    @Test
    public void testInit()
    {
        commandDispatcher.init();

        verify( peerManager ).addPeerMessageListener( commandDispatcher );
    }


    @Test
    public void testDispose()
    {
        commandDispatcher.destroy();

        verify( peerManager ).removePeerMessageListener( commandDispatcher );
    }


    @Test
    public void testGetName()
    {
        assertEquals( Common.DISPATCHER_NAME, commandDispatcher.getName() );
    }


    @Test
    public void testOnMessage() throws PeerMessageException
    {
        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( peerId );
        Request request =
                new Request( "source", RequestType.EXECUTE_REQUEST, agentId, commandId, null, null, null, null, null,
                        null, null, null, null, null, null, timeout );
        BatchRequest batchRequest = new BatchRequest( request, agentId, commandId );
        DispatcherMessage message =
                new DispatcherMessage( DispatcherMessageType.REQUEST, Sets.newHashSet( batchRequest ) );

        commandDispatcher.onMessage( peer, JsonUtil.toJson( message ) );


        verify( commandRunner ).runCommandAsync( any( Command.class ), any( CommandCallback.class ) );
    }
}
