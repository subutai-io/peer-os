package io.subutai.core.localpeer.rest;


import java.util.UUID;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
@Ignore
public class RestServiceImplTest
{
    private static final String PEER_ID = UUID.randomUUID().toString();
    private static final String JSON = "json";
    private static final String ENTITY = "entity";
    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static final String TEMPLATE_NAME = "master";

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
    @Mock
    PeerException peerException;
    @Mock
    ContainerHost containerHost;
    @Mock
    ResourceHost managementHost;
    @Mock
    SecurityManager securityManager;

    RestServiceImpl restService;

    @Mock
    ContainerId containerId;


    @Before
    public void setUp() throws Exception
    {
        restService = spy( new RestServiceImpl( localPeer ) );
        restService.jsonUtil = jsonUtil;
        when( containerId.getId() ).thenReturn( CONTAINER_ID );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getId() ).thenReturn( PEER_ID );
        when( localPeer.getPeerInfo() ).thenReturn( peerInfo );
        when( localPeer.bindHost( CONTAINER_ID ) ).thenReturn( containerHost );
        when( peerManager.getPeer( PEER_ID ) ).thenReturn( peer );
        when( peer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getId() ).thenReturn( PEER_ID );
        when( restUtil.getTrustedWebClient( anyString(), any() ) ).thenReturn( webClient );
        when( webClient.type( anyString() ) ).thenReturn( webClient );
        when( jsonUtil.to( anyObject() ) ).thenReturn( JSON );
        when( webClient.path( anyString() ) ).thenReturn( webClient );
        when( webClient.form( any( Form.class ) ) ).thenReturn( response );
        when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
        when( response.readEntity( String.class ) ).thenReturn( ENTITY );
        when( jsonUtil.from( PEER_ID, UUID.class ) ).thenCallRealMethod();
        when( jsonUtil.from( CONTAINER_ID, UUID.class ) ).thenCallRealMethod();
        when( jsonUtil.from( JSON, PeerInfo.class ) ).thenReturn( peerInfo );
        when( localPeer.getContainerHostById( CONTAINER_ID ) ).thenReturn( containerHost );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
    }


    @Test
    public void testGetTemplate() throws Exception
    {
        TemplateKurjun template = mock( TemplateKurjun.class );

        when( localPeer.getTemplate( TEMPLATE_NAME ) ).thenReturn( template );

        restService.getTemplate( TEMPLATE_NAME );

        verify( jsonUtil ).to( template );

        doThrow( exception ).when( localPeer ).getTemplate( TEMPLATE_NAME );

        Response response1 = restService.getTemplate( TEMPLATE_NAME );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getStatus() );
    }
}
