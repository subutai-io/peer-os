package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;

import com.google.common.collect.Sets;
import com.jayway.awaitility.Awaitility;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for CommandDispatcherImpl
 */
@RunWith(MockitoJUnitRunner.class)
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
    private static final UUID agentId = UUIDUtil.generateTimeBasedUUID();
    private static final UUID commandId = UUIDUtil.generateTimeBasedUUID();
    private static final UUID peerId = UUIDUtil.generateTimeBasedUUID();
    private static final Integer timeout = 30;
    private static final Request request =
            new Request( "source", RequestType.EXECUTE_REQUEST, agentId, commandId, null, null, null, null, null, null,
                    null, null, null, null, null, timeout );


    @Before
    public void setUp()
    {
        commandDispatcher = new CommandDispatcherImpl( agentManager, commandRunner, dbManager, peerManager );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNullAgentManager()
    {
        new CommandDispatcherImpl( null, commandRunner, dbManager, peerManager );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNullCommandRunner()
    {
        new CommandDispatcherImpl( agentManager, null, dbManager, peerManager );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNullDbManager()
    {
        new CommandDispatcherImpl( agentManager, commandRunner, null, peerManager );
    }


    @Test(expected = NullPointerException.class)
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
    public void testOnMessageForRequests() throws PeerMessageException
    {
        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( peerId );

        BatchRequest batchRequest = new BatchRequest( request, agentId, commandId );
        DispatcherMessage message =
                new DispatcherMessage( DispatcherMessageType.REQUEST, Sets.newHashSet( batchRequest ) );

        commandDispatcher.onMessage( peer, JsonUtil.toJson( message ) );


        verify( commandRunner ).runCommandAsync( any( Command.class ), any( CommandCallback.class ) );
    }


    @Test
    public void testExecuteCommand()
    {

        CommandImpl command = mock( CommandImpl.class );

        when( command.getRequests() ).thenReturn( Sets.newHashSet( request ) );
        when( command.getCommandUUID() ).thenReturn( commandId );
        Agent agent = mock( Agent.class );
        when( agentManager.getAgentByUUID( agentId ) ).thenReturn( agent );


        commandDispatcher.runCommandAsync( command, new CommandCallback()
        {
            @Override
            public void onResponse( final Response response, final AgentResult agentResult, final Command command )
            {

            }
        } );

        verify( commandRunner ).runCommandAsync( any( Command.class ), any( CommandCallback.class ) );
    }


    @Test
    public void testOnMessageForResponses() throws PeerMessageException
    {


        CommandImpl command = mock( CommandImpl.class );

        when( command.getRequests() ).thenReturn( Sets.newHashSet( request ) );
        when( command.getCommandUUID() ).thenReturn( commandId );
        Agent agent = mock( Agent.class );
        when( agentManager.getAgentByUUID( agentId ) ).thenReturn( agent );

        final AtomicBoolean callbackTriggered = new AtomicBoolean( false );

        commandDispatcher.runCommandAsync( command, new CommandCallback()
        {
            @Override
            public void onResponse( final Response response, final AgentResult agentResult, final Command command )
            {
                callbackTriggered.set( true );
            }
        } );


        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( peerId );
        String responseString = String.format( "{uuid:%s,taskUuid:%s,responseSequenceNumber:%s}", agentId.toString(),
                commandId.toString(), 1 );
        Response response = JsonUtil.fromJson( responseString, Response.class );

        DispatcherMessage message =
                new DispatcherMessage( Sets.newHashSet( response ), DispatcherMessageType.RESPONSE );

        commandDispatcher.onMessage( peer, JsonUtil.toJson( message ) );


        Awaitility.await().atMost( 2, TimeUnit.SECONDS ).with().pollInterval( 100, TimeUnit.MILLISECONDS )
                  .untilTrue( callbackTriggered );
    }
}
