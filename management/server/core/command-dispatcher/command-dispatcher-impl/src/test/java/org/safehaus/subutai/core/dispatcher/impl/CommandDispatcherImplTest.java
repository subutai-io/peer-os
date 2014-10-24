package org.safehaus.subutai.core.dispatcher.impl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.ContainerRequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.RunCommandException;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;

import com.google.common.collect.Sets;
import com.jayway.awaitility.Awaitility;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    DataSource dataSource;
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
    private static final UUID environmentId = UUID.randomUUID();
    private static final UUID remotePeerId = UUID.randomUUID();


    @Before
    public void setUp() throws DaoException, SQLException
    {
        Connection connection = mock( Connection.class );
        PreparedStatement preparedStatement = mock( PreparedStatement.class );
        when( connection.prepareStatement( anyString() ) ).thenReturn( preparedStatement );
        when( dataSource.getConnection() ).thenReturn( connection );
        ResultSet resultSet = mock( ResultSet.class );
        when(preparedStatement.executeQuery()).thenReturn( resultSet );
        ResultSetMetaData metadata = mock( ResultSetMetaData.class);
        when(metadata.getColumnCount()).thenReturn( 1 );
        when(resultSet.getMetaData()).thenReturn( metadata);
        commandDispatcher = new CommandDispatcherImpl( agentManager, commandRunner, peerManager, dataSource );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNullAgentManager() throws DaoException
    {
        new CommandDispatcherImpl( null, commandRunner, peerManager, dataSource );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNullCommandRunner() throws DaoException
    {
        new CommandDispatcherImpl( agentManager, null, peerManager, dataSource );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNullDatasource() throws DaoException
    {
        new CommandDispatcherImpl( agentManager, commandRunner, peerManager, null );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNullPeerManager() throws DaoException
    {
        new CommandDispatcherImpl( agentManager, commandRunner, null, dataSource );
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


        commandDispatcher.runCommandAsync( command, MockUtils.getDummyCallback() );


        verify( commandRunner ).runCommandAsync( any( Command.class ), any( CommandCallback.class ) );
    }


    @Test
    public void testOnMessageForResponses() throws PeerMessageException
    {


        //setup callback to be triggered
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


    @Test
    public void testExecuteRemoteCommand() throws PeerException, PeerMessageException
    {

        CommandImpl command = mock( CommandImpl.class );

        BatchRequest batchRequest = new BatchRequest( request, agentId, environmentId );
        Map<UUID, Set<BatchRequest>> remoteRequests = new HashMap<>();
        remoteRequests.put( remotePeerId, Sets.newHashSet( batchRequest ) );
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentId );
        when( command.getRemoteRequests() ).thenReturn( remoteRequests );
        when( command.getCommandUUID() ).thenReturn( commandId );
        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( remotePeerId );
        when( peerManager.getPeerByUUID( remotePeerId ) ).thenReturn( peer );
        when( peerManager.isPeerReachable( peer ) ).thenReturn( true );
        when( peerManager.getConnectedAgents( peer, environmentId.toString() ) ).thenReturn( Sets.newHashSet( agent ) );


        commandDispatcher.runCommandAsync( command, MockUtils.getDummyCallback() );


        verify( peerManager ).sendPeerMessage( eq( peer ), eq( Common.DISPATCHER_NAME ), anyString() );
    }


    @Test
    public void shouldCreateContainerCommand()
    {
        RequestBuilder requestBuilder = mock( RequestBuilder.class );
        Container container = mock( Container.class );
        when( peerManager.getSiteId() ).thenReturn( peerId );
        when( container.getPeerId() ).thenReturn( peerId );
        when( container.getAgentId() ).thenReturn( agentId );
        when( requestBuilder.build( eq( agentId ), any( UUID.class ) ) ).thenReturn( request );


        CommandImpl command = ( CommandImpl ) commandDispatcher
                .createContainerCommand( requestBuilder, Sets.newHashSet( container ) );


        assertEquals( request, command.getRequests().iterator().next() );
    }


    @Test
    public void shouldCreateContainerCommand2()
    {
        ContainerRequestBuilder requestBuilder = mock( ContainerRequestBuilder.class );
        Container container = mock( Container.class );
        when( peerManager.getSiteId() ).thenReturn( peerId );
        when( container.getPeerId() ).thenReturn( remotePeerId );
        when( container.getAgentId() ).thenReturn( agentId );
        when( container.getEnvironmentId() ).thenReturn( environmentId );
        when( requestBuilder.build( any( UUID.class ) ) ).thenReturn( request );
        when( requestBuilder.getContainer() ).thenReturn( container );


        CommandImpl command =
                ( CommandImpl ) commandDispatcher.createContainerCommand( Sets.newHashSet( requestBuilder ) );


        assertEquals( request,
                command.getRemoteRequests().values().iterator().next().iterator().next().getRequests().iterator()
                       .next() );
    }


    @Test
    public void shouldCreateContainerCommand3()
    {
        RequestBuilder requestBuilder = mock( RequestBuilder.class );
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentId );
        when( peerManager.getSiteId() ).thenReturn( peerId );
        when( agent.getSiteId() ).thenReturn( peerId );
        when( requestBuilder.build( eq( agentId ), any( UUID.class ) ) ).thenReturn( request );


        CommandImpl command =
                ( CommandImpl ) commandDispatcher.createCommand( requestBuilder, Sets.newHashSet( agent ) );


        assertEquals( request, command.getRequests().iterator().next() );
    }


    @Test
    public void shouldCreateContainerCommand4()
    {
        AgentRequestBuilder requestBuilder = mock( AgentRequestBuilder.class );
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentId );
        when( peerManager.getSiteId() ).thenReturn( peerId );
        when( agent.getSiteId() ).thenReturn( remotePeerId );
        when( agent.getEnvironmentId() ).thenReturn( environmentId );
        when( requestBuilder.getAgent() ).thenReturn( agent );
        when( requestBuilder.build( any( UUID.class ) ) ).thenReturn( request );


        CommandImpl command = ( CommandImpl ) commandDispatcher.createCommand( Sets.newHashSet( requestBuilder ) );


        assertEquals( request,
                command.getRemoteRequests().values().iterator().next().iterator().next().getRequests().iterator()
                       .next() );
    }


    @Test(expected = PeerMessageException.class)
    public void testOnMessageForRequestsThrowDbException() throws PeerMessageException, DaoException, SQLException
    {
        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( peerId );
        BatchRequest batchRequest = new BatchRequest( request, agentId, commandId );
        DispatcherMessage message =
                new DispatcherMessage( DispatcherMessageType.REQUEST, Sets.newHashSet( batchRequest ) );
        Mockito.doThrow( new SQLException( new Throwable() ) ).when( dataSource ).getConnection();


        commandDispatcher.onMessage( peer, JsonUtil.toJson( message ) );
    }


    @Test(expected = PeerMessageException.class)
    public void shouldThrowPeerException() throws PeerMessageException, DaoException
    {

        commandDispatcher.onMessage( mock( Peer.class ), "" );
    }


    @Test(expected = RunCommandException.class)
    public void shouldThrowRunCommandException()
    {

        CommandImpl command = mock( CommandImpl.class );

        when( command.getRequests() ).thenReturn( Sets.newHashSet( request ) );
        when( command.getCommandUUID() ).thenReturn( commandId );


        commandDispatcher.runCommandAsync( command, MockUtils.getDummyCallback() );
    }


    @Test(expected = RunCommandException.class)
    public void shouldThrowRunCommandException2() throws PeerException, PeerMessageException
    {

        CommandImpl command = mock( CommandImpl.class );

        BatchRequest batchRequest = new BatchRequest( request, agentId, environmentId );
        Map<UUID, Set<BatchRequest>> remoteRequests = new HashMap<>();
        remoteRequests.put( remotePeerId, Sets.newHashSet( batchRequest ) );
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentId );
        when( command.getRemoteRequests() ).thenReturn( remoteRequests );
        when( command.getCommandUUID() ).thenReturn( commandId );
        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( remotePeerId );
        when( peerManager.getPeerByUUID( remotePeerId ) ).thenReturn( peer );
        when( peerManager.isPeerReachable( peer ) ).thenReturn( true );
        when( peerManager.getConnectedAgents( peer, environmentId.toString() ) ).thenReturn( Sets.newHashSet( agent ) );
        when( peerManager.sendPeerMessage( eq( peer ), eq( Common.DISPATCHER_NAME ), anyString() ) )
                .thenThrow( new PeerMessageException( "" ) );

        commandDispatcher.runCommandAsync( command, MockUtils.getDummyCallback() );
    }


    @Test(expected = RunCommandException.class)
    public void shouldThrowRunCommandException3() throws PeerException, PeerMessageException
    {

        CommandImpl command = mock( CommandImpl.class );

        BatchRequest batchRequest = new BatchRequest( request, agentId, environmentId );
        Map<UUID, Set<BatchRequest>> remoteRequests = new HashMap<>();
        remoteRequests.put( remotePeerId, Sets.newHashSet( batchRequest ) );
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentId );
        when( command.getRemoteRequests() ).thenReturn( remoteRequests );
        when( command.getCommandUUID() ).thenReturn( commandId );
        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( remotePeerId );
        when( peerManager.getPeerByUUID( remotePeerId ) ).thenReturn( null );
        when( peerManager.isPeerReachable( peer ) ).thenReturn( true );
        when( peerManager.getConnectedAgents( peer, environmentId.toString() ) ).thenReturn( Sets.newHashSet( agent ) );

        commandDispatcher.runCommandAsync( command, MockUtils.getDummyCallback() );
    }


    @Test(expected = RunCommandException.class)
    public void shouldThrowRunCommandException4() throws PeerException, PeerMessageException
    {

        CommandImpl command = mock( CommandImpl.class );

        BatchRequest batchRequest = new BatchRequest( request, agentId, environmentId );
        Map<UUID, Set<BatchRequest>> remoteRequests = new HashMap<>();
        remoteRequests.put( remotePeerId, Sets.newHashSet( batchRequest ) );
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentId );
        when( command.getRemoteRequests() ).thenReturn( remoteRequests );
        when( command.getCommandUUID() ).thenReturn( commandId );
        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( remotePeerId );
        when( peerManager.getPeerByUUID( remotePeerId ) ).thenReturn( peer );
        Mockito.doThrow( new PeerException( "" ) ).when( peerManager ).isPeerReachable( peer );

        commandDispatcher.runCommandAsync( command, MockUtils.getDummyCallback() );
    }


    @Test(expected = RunCommandException.class)
    public void shouldThrowRunCommandException5() throws PeerException, PeerMessageException
    {

        CommandImpl command = mock( CommandImpl.class );

        BatchRequest batchRequest = new BatchRequest( request, agentId, environmentId );
        Map<UUID, Set<BatchRequest>> remoteRequests = new HashMap<>();
        remoteRequests.put( remotePeerId, Sets.newHashSet( batchRequest ) );
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentId );
        when( command.getRemoteRequests() ).thenReturn( remoteRequests );
        when( command.getCommandUUID() ).thenReturn( commandId );
        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( remotePeerId );
        when( peerManager.getPeerByUUID( remotePeerId ) ).thenReturn( peer );
        when( peerManager.isPeerReachable( peer ) ).thenReturn( true );
        when( peerManager.getConnectedAgents( peer, environmentId.toString() ) )
                .thenReturn( Collections.<Agent>emptySet() );

        commandDispatcher.runCommandAsync( command, MockUtils.getDummyCallback() );
    }


    @Test(expected = RunCommandException.class)
    public void shouldThrowRunCommandException6() throws PeerException, PeerMessageException
    {

        CommandImpl command = mock( CommandImpl.class );

        BatchRequest batchRequest = new BatchRequest( request, agentId, environmentId );
        Map<UUID, Set<BatchRequest>> remoteRequests = new HashMap<>();
        remoteRequests.put( remotePeerId, Sets.newHashSet( batchRequest ) );
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentId );
        when( command.getRemoteRequests() ).thenReturn( remoteRequests );
        when( command.getCommandUUID() ).thenReturn( commandId );
        Peer peer = mock( Peer.class );
        when( peer.getId() ).thenReturn( remotePeerId );
        when( peerManager.getPeerByUUID( remotePeerId ) ).thenReturn( peer );
        when( peerManager.isPeerReachable( peer ) ).thenReturn( false );
        when( peerManager.getConnectedAgents( peer, environmentId.toString() ) ).thenReturn( Sets.newHashSet( agent ) );

        commandDispatcher.runCommandAsync( command, MockUtils.getDummyCallback() );
    }


    @Test(expected = RunCommandException.class)
    public void shouldThrowRunCommandException7()
    {
        commandDispatcher.saveResponse( null );
    }
}
