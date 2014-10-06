package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for ResponseSender
 */
public class ResponseSenderTest
{

    PeerManager peerManager;
    DispatcherDAO dispatcher;

    ResponseSender responseSender;
    private static final UUID agentId = UUID.randomUUID();
    private static final int ATTEMPTS = 1;


    @Before
    public void setUp()
    {
        peerManager = mock( PeerManager.class );
        dispatcher = mock( DispatcherDAO.class );
        responseSender = new ResponseSender( dispatcher, peerManager );
        responseSender.init();
    }


    @After
    public void tearDown() throws Exception
    {
        responseSender.dispose();
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullDAO()
    {
        new ResponseSender( null, peerManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullPeerManager()
    {
        new ResponseSender( dispatcher, null );
    }


    @Test
    public void testSend() throws DBException
    {

        RemoteRequest request = mock( RemoteRequest.class );
        when( dispatcher.getRemoteRequests( anyInt(), anyInt() ) ).thenReturn( Sets.newHashSet( request ) );

        responseSender.send();

        verify( dispatcher ).deleteRemoteRequest( any( UUID.class ) );
        verify( dispatcher ).deleteRemoteResponses( any( UUID.class ) );
    }


    @Test
    public void testSend2() throws DBException
    {

        RemoteRequest request = mock( RemoteRequest.class );
        when( request.getTimestamp() ).thenReturn(
                System.currentTimeMillis() - ( ResponseSender.AGENT_CHUNK_SEND_INTERVAL_SEC + 5 ) * 1000L * 2 );
        when( dispatcher.getRemoteRequests( anyInt(), anyInt() ) ).thenReturn( Sets.newHashSet( request ) );

        responseSender.send();

        verify( dispatcher ).saveRemoteRequest( any( RemoteRequest.class ) );
        verify( dispatcher ).deleteRemoteRequest( any( UUID.class ), anyInt() );
    }


    @Test
    public void testSend3() throws DBException, InterruptedException
    {
        ExecutorService executorService = mock( ExecutorService.class );
        responseSender.setHttpRequestsExecutor( executorService );
        RemoteRequest request = mock( RemoteRequest.class );
        when( dispatcher.getRemoteRequests( anyInt(), anyInt() ) ).thenReturn( Sets.newHashSet( request ) );
        RemoteResponse remoteResponse = mock( RemoteResponse.class );
        RemoteResponse remoteResponse2 = mock( RemoteResponse.class );

        Response response =
                JsonUtil.fromJson( String.format( "{responseSequenceNumber:%s,uuid=%s}", 1, agentId ), Response.class );
        Response response2 = JsonUtil.fromJson(
                String.format( "{responseSequenceNumber:%s, type:%s, uuid:%s}", 2, ResponseType.EXECUTE_RESPONSE_DONE,
                        agentId ), Response.class );

        when( remoteResponse.getResponse() ).thenReturn( response );
        when( remoteResponse2.getResponse() ).thenReturn( response2 );
        when( dispatcher.getRemoteResponses( any( UUID.class ) ) )
                .thenReturn( Sets.newHashSet( remoteResponse, remoteResponse2 ) );

        responseSender.send();

        verify( executorService ).invokeAll( anyCollection() );
    }


    @Test
    public void testSend4() throws DBException, PeerMessageException
    {
        RemoteRequest request = mock( RemoteRequest.class );
        when( dispatcher.getRemoteRequests( anyInt(), anyInt() ) ).thenReturn( Sets.newHashSet( request ) );
        RemoteResponse remoteResponse = mock( RemoteResponse.class );
        RemoteResponse remoteResponse2 = mock( RemoteResponse.class );

        Response response =
                JsonUtil.fromJson( String.format( "{responseSequenceNumber:%s,uuid=%s}", 1, agentId ), Response.class );
        Response response2 = JsonUtil.fromJson(
                String.format( "{responseSequenceNumber:%s, type:%s, uuid:%s}", 2, ResponseType.EXECUTE_RESPONSE_DONE,
                        agentId ), Response.class );

        when( remoteResponse.getResponse() ).thenReturn( response );
        when( remoteResponse2.getResponse() ).thenReturn( response2 );
        when( dispatcher.getRemoteResponses( any( UUID.class ) ) )
                .thenReturn( Sets.newHashSet( remoteResponse, remoteResponse2 ) );


        responseSender.send();

        verify( peerManager, atLeastOnce() ).sendPeerMessage( any( Peer.class ), anyString(), anyString() );
        verify( dispatcher, atLeastOnce() ).deleteRemoteResponse( any( RemoteResponse.class ) );
        verify( dispatcher , atLeastOnce()).saveRemoteRequest( any( RemoteRequest.class ) );
    }


    @Test
    public void testSend5() throws DBException, PeerMessageException
    {
        RemoteRequest request = mock( RemoteRequest.class );
        when( request.isCompleted() ).thenReturn( true );
        when( dispatcher.getRemoteRequests( anyInt(), anyInt() ) ).thenReturn( Sets.newHashSet( request ) );
        RemoteResponse remoteResponse = mock( RemoteResponse.class );
        RemoteResponse remoteResponse2 = mock( RemoteResponse.class );

        Response response =
                JsonUtil.fromJson( String.format( "{responseSequenceNumber:%s,uuid=%s}", 1, agentId ), Response.class );
        Response response2 = JsonUtil.fromJson(
                String.format( "{responseSequenceNumber:%s, type:%s, uuid:%s}", 2, ResponseType.EXECUTE_RESPONSE_DONE,
                        agentId ), Response.class );

        when( remoteResponse.getResponse() ).thenReturn( response );
        when( remoteResponse2.getResponse() ).thenReturn( response2 );
        when( dispatcher.getRemoteResponses( any( UUID.class ) ) )
                .thenReturn( Sets.newHashSet( remoteResponse, remoteResponse2 ) );


        responseSender.send();

        verify( peerManager, atLeastOnce() ).sendPeerMessage( any( Peer.class ), anyString(), anyString() );
        verify( dispatcher, atLeastOnce() ).deleteRemoteResponse( any( RemoteResponse.class ) );
        verify( dispatcher, atLeastOnce() ).deleteRemoteRequest( any( UUID.class ) );
    }


    @Test
    public void testSend6() throws DBException, PeerMessageException
    {
        RemoteRequest request = mock( RemoteRequest.class );
        when( request.getAttempts() ).thenReturn( ATTEMPTS );
        when( request.isCompleted() ).thenReturn( true );
        when( dispatcher.getRemoteRequests( anyInt(), anyInt() ) ).thenReturn( Sets.newHashSet( request ) );
        RemoteResponse remoteResponse = mock( RemoteResponse.class );
        RemoteResponse remoteResponse2 = mock( RemoteResponse.class );

        Response response =
                JsonUtil.fromJson( String.format( "{responseSequenceNumber:%s,uuid=%s}", 1, agentId ), Response.class );
        Response response2 = JsonUtil.fromJson(
                String.format( "{responseSequenceNumber:%s, type:%s, uuid:%s}", 2, ResponseType.EXECUTE_RESPONSE_DONE,
                        agentId ), Response.class );

        when( remoteResponse.getResponse() ).thenReturn( response );
        when( remoteResponse2.getResponse() ).thenReturn( response2 );
        when( dispatcher.getRemoteResponses( any( UUID.class ) ) )
                .thenReturn( Sets.newHashSet( remoteResponse, remoteResponse2 ) );

        when( peerManager.sendPeerMessage( any( Peer.class ), anyString(), anyString() ) )
                .thenThrow( new PeerMessageException( "" ) );


        responseSender.send();

        verify( request, atLeastOnce() ).incrementAttempts();
        verify( dispatcher , atLeastOnce()).saveRemoteRequest( any( RemoteRequest.class ) );
        verify( dispatcher, atLeastOnce() ).deleteRemoteRequest( any( UUID.class ), eq( ATTEMPTS - 1 ) );
    }
}
