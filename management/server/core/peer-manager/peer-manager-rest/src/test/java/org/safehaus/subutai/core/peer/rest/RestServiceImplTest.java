package org.safehaus.subutai.core.peer.rest;


import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.peer.PeerPolicy;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.ssl.manager.api.SubutaiSslContextFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final String JSON = "json";
    private static final String PEER_IP = "127.0.0.1";
    private static final String ENTITY = "entity";
    @Mock
    SubutaiSslContextFactory sslContextFactory;
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    PeerInfo peerInfo;
    @Mock
    PeerPolicy peerPolicy;
    @Mock
    JsonUtil jsonUtil;
    @Mock
    Peer peer;
    @Mock
    RestUtil restUtil;
    @Mock
    WebClient webClient;
    @Mock
    Response response;
    @Mock
    RuntimeException exception;

    RestServiceImpl restService;


    @Before
    public void setUp() throws Exception
    {
        restService = spy( new RestServiceImpl( peerManager, sslContextFactory ) );
        restService.jsonUtil = jsonUtil;
        restService.restUtil = restUtil;
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getId() ).thenReturn( PEER_ID );
        when( localPeer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getPeerPolicy( any( UUID.class ) ) ).thenReturn( peerPolicy );
        when( peerManager.getPeer( PEER_ID.toString() ) ).thenReturn( peer );
        when( peer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getId() ).thenReturn( PEER_ID );
        when( peerManager.getPeerInfo( PEER_ID ) ).thenReturn( peerInfo );
        when( restUtil.getTrustedWebClient( anyString() ) ).thenReturn( webClient );
        when( webClient.type( anyString() ) ).thenReturn( webClient );
        when( peerManager.getLocalPeerInfo() ).thenReturn( peerInfo );
        when( jsonUtil.to( anyObject() ) ).thenReturn( JSON );
        when( webClient.path( anyString() ) ).thenReturn( webClient );
        when( webClient.form( any( Form.class ) ) ).thenReturn( response );
        when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
        when( response.readEntity( String.class ) ).thenReturn( ENTITY );
    }


    @Test
    public void testGetSelfPeerInfo() throws Exception
    {
        restService.getSelfPeerInfo();

        verify( peerManager ).getLocalPeerInfo();
    }


    @Test
    public void testGetId() throws Exception
    {
        restService.getId();

        verify( localPeer ).getId();
    }


    @Test
    public void testGetRegisteredPeers() throws Exception
    {
        restService.getRegisteredPeers();

        verify( peerManager ).getPeerInfos();
    }


    @Test
    public void testGetPeerPolicy() throws Exception
    {


        restService.getPeerPolicy( PEER_ID.toString() );

        verify( jsonUtil ).to( peerPolicy );


        reset( jsonUtil );

        when( peerInfo.getPeerPolicy( PEER_ID ) ).thenReturn( null );

        Response response = restService.getPeerPolicy( PEER_ID.toString() );

        assertNull( response.getEntity() );
    }


    @Test
    public void testGetRegisteredPeerInfo() throws Exception
    {
        restService.getRegisteredPeerInfo( PEER_ID.toString() );

        verify( jsonUtil ).to( peerInfo );
    }


    @Test
    public void testPing() throws Exception
    {
        Response response = restService.ping();

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testProcessRegisterRequest() throws Exception
    {
        when( jsonUtil.from( JSON, PeerInfo.class ) ).thenReturn( peerInfo );

        Response response = restService.processRegisterRequest( JSON );

        assertEquals( Response.Status.CONFLICT.getStatusCode(), response.getStatus() );

        when( peerManager.getPeerInfo( PEER_ID ) ).thenReturn( null );

        restService.processRegisterRequest( JSON );

        verify( peerManager ).register( peerInfo );

        doThrow( new PeerException( "" ) ).when( peerManager ).register( peerInfo );

        response = restService.processRegisterRequest( JSON );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testSendRegistrationRequest() throws Exception
    {
        restService.sendRegistrationRequest( PEER_IP );

        verify( restService ).registerPeerCert( response );

        when( response.getStatus() ).thenReturn( Response.Status.CONFLICT.getStatusCode() );

        Response response1 = restService.sendRegistrationRequest( PEER_IP );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getStatus() );
        assertEquals( ENTITY, response1.getEntity() );

        when( response.getStatus() ).thenReturn( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() );

        response1 = restService.sendRegistrationRequest( PEER_IP );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getStatus() );

        doThrow( exception ).when( webClient ).path( anyString() );

        response1 = restService.sendRegistrationRequest( PEER_IP );

        assertEquals( exception.toString(), response1.getEntity() );
    }
}
