package io.subutai.core.localpeer.rest;


import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;

import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerGateway;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ManagementHost;
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
import static junit.framework.TestCase.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
@Ignore
public class RestServiceImplTest
{
    private static final String PEER_ID = UUID.randomUUID().toString();
    private static final String JSON = "json";
    private static final String IP = "127.0.0.1";
    private static final String ENTITY = "entity";
    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static final String TEMPLATE_NAME = "master";
    private static final int PID = 123;
    private static final int QUOTA = 123;

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

    @Mock
    private ContainerGateway containerGateway;

    @Mock
    Vni vni;


    @Before
    public void setUp() throws Exception
    {
        restService = spy( new RestServiceImpl( /*peerManager, httpContextManager, securityManager,*/  null ) );
        restService.jsonUtil = jsonUtil;
        restService.restUtil = restUtil;
        when( containerId.getId() ).thenReturn( CONTAINER_ID );
        when( containerGateway.getContainerId() ).thenReturn( containerId );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getId() ).thenReturn( PEER_ID );
        when( localPeer.getPeerInfo() ).thenReturn( peerInfo );
        when( localPeer.bindHost( CONTAINER_ID.toString() ) ).thenReturn( containerHost );
//        when( peerInfo.getPeerPolicy(  ) ).thenReturn( peerPolicy );
        when( peerManager.getPeer( PEER_ID.toString() ) ).thenReturn( peer );
        when( peer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getId() ).thenReturn( PEER_ID );
//        when( peerManager.getPeerInfo( PEER_ID ) ).thenReturn( peerInfo );
        when( restUtil.getTrustedWebClient( anyString(), any() ) ).thenReturn( webClient );
        when( webClient.type( anyString() ) ).thenReturn( webClient );
//        when( peerManager.getLocalPeerInfo() ).thenReturn( peerInfo );
        when( jsonUtil.to( anyObject() ) ).thenReturn( JSON );
        when( webClient.path( anyString() ) ).thenReturn( webClient );
        when( webClient.form( any( Form.class ) ) ).thenReturn( response );
        when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
        when( response.readEntity( String.class ) ).thenReturn( ENTITY );
        when( jsonUtil.from( PEER_ID.toString(), UUID.class ) ).thenCallRealMethod();
        when( jsonUtil.from( CONTAINER_ID.toString(), UUID.class ) ).thenCallRealMethod();
        when( jsonUtil.from( JSON, PeerInfo.class ) ).thenReturn( peerInfo );
        when( localPeer.getContainerHostById( CONTAINER_ID ) ).thenReturn( containerHost );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
    }

    //
    //    @Test
    //    public void testGetSelfPeerInfo() throws Exception
    //    {
    //        restService.getLocalPeerInfo();
    //
    //        verify( peerManager ).getLocalPeerInfo();
    //    }


    //    @Test
    //    public void testGetId() throws Exception
    //    {
    //        restService.getInfo();
    //
    //        verify( localPeer ).getId();
    //    }

    //
    //    @Test
    //    public void testGetRegisteredPeers() throws Exception
    //    {
    //        restService.getRegisteredPeers();
    //
    //        verify( peerManager ).getPeerInfos();
    //    }

//
//    @Test
//    public void testGetPeerPolicy() throws Exception
//    {
//
//
//        restService.getPeerPolicy(  );
//
//        verify( jsonUtil ).to( peerPolicy );
//
//
//        reset( jsonUtil );
//
//        when( peerInfo.getPeerPolicy(  ) ).thenReturn( null );
//
//        Response response = restService.getPeerPolicy( );
//
//        assertNull( response.getEntity() );
//    }

    //
    //    @Test
    //    public void testGetRegisteredPeerInfo() throws Exception
    //    {
    //        restService.getRegisteredPeerInfo( PEER_ID.toString() );
    //
    //        verify( jsonUtil ).to( peerInfo );
    //    }


    //    @Test
    //    public void testPing() throws Exception
    //    {
    //        Response response = restService.ping();
    //
    //        assertEquals( Response.Status.OK.getStatusCode(), response.getState() );
    //    }
    //


    @Test
    public void testApproveForRegistrationRequest() throws Exception
    {
        //restService.approveForRegistrationRequest( JSON, CERT );

        //verify( httpContextManager ).reloadTrustStore();

//        doThrow( exception ).when( peerManager ).update( peerInfo );

        //Response response1 = restService.approveForRegistrationRequest( JSON, CERT );

        //assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    }


    @Test
    public void testUpdatePeer() throws Exception
    {
        doReturn( IP ).when( restService ).getRequestIp();

        //restService.updatePeer( JSON, CERT );

        //verify( peerManager ).update( peerInfo );

//        doThrow( exception ).when( peerManager ).update( peerInfo );

        //Response response1 = restService.updatePeer( JSON, CERT );

        //assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    }

    //
    //    @Test
    //    public void testDestroyContainer() throws Exception
    //    {
    //        restService.destroyContainer( containerId );
    //
    //        verify( peerManager ).destroyContainer( containerId );
    //
    //        doThrow( exception ).when( localPeer ).bindHost( containerId );
    //
    //        restService.destroyContainer( containerId );
    //    }
    //
    //
    //    @Test
    //    public void testStartContainer() throws Exception
    //    {
    //        restService.startContainer( containerId );
    //
    //        verify( peerManager ).startContainer( containerId );
    //
    //        doThrow( exception ).when( localPeer ).bindHost( containerId );
    //
    //        restService.startContainer( containerId );
    //    }


    //    @Test
    //    public void testStopContainer() throws Exception
    //    {
    //        restService.stopContainer( containerId );
    //
    //        verify( peerManager ).stopContainer( containerId );
    //
    //
    //        doThrow( exception ).when( localPeer ).bindHost( containerId );
    //
    //        restService.stopContainer( containerId );
    //    }


    //    @Test
    //    public void testIsContainerConnected() throws Exception
    //    {
    //        when( containerHost.isConnected() ).thenReturn( true );
    //
    //        Response response1 = restService.isConnected( CONTAINER_ID.toString() );
    //
    //        assertTrue( Boolean.valueOf( response1.readEntity( String.class ) ) );
    //
    //        doThrow( exception ).when( localPeer ).bindHost( CONTAINER_ID.toString() );
    //
    //        response1 = restService.getContainerState( containerId ) isContainerConnected( CONTAINER_ID.toString() );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }


    //    @Test
    //    public void testGetContainerState() throws Exception
    //    {
    //        when( containerHost.getState() ).thenReturn( ContainerHostState.RUNNING );
    //
    //        restService.getContainerState( containerId );
    //
    //        verify( peerManager ).getContainerState( containerId );
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        restService.getContainerState( containerId );
    //
    //        //assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }


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


    //    @Test
    //    public void testGetAvailableRamQuota() throws Exception
    //    {
    //        restService.getAvailableRamQuota( CONTAINER_ID.toString() );
    //
    //        verify( containerHost ).getAvailableRamQuota();
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.getAvailableRamQuota( CONTAINER_ID.toString() );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testGetAvailableCpuQuota() throws Exception
    //    {
    //        restService.getAvailableCpuQuota( CONTAINER_ID.toString() );
    //
    //        verify( containerHost ).getAvailableCpuQuota();
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.getAvailableCpuQuota( CONTAINER_ID.toString() );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testGetAvailableDiskQuota() throws Exception
    //    {
    //        restService.getAvailableDiskQuota( CONTAINER_ID.toString(), JSON );
    //
    //        verify( containerHost ).getAvailableDiskQuota( any( DiskPartition.class ) );
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.getAvailableDiskQuota( CONTAINER_ID.toString(), JSON );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    //    @Test
    //    //    public void testGetProcessResourceUsage() throws Exception
    //    //    {
    //    //        restService.getProcessResourceUsage( containerId, PID );
    //    //
    //    //        verify( peerManager ).getProcessResourceUsage( containerId, PID );
    //    //    }
    //
    //
    //    @Test
    //    public void testGetRamQuota() throws Exception
    //    {
    //        restService.getRamQuota( CONTAINER_ID.toString() );
    //
    //        verify( containerHost ).getRamQuota();
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.getRamQuota( CONTAINER_ID.toString() );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testGetRamQuotaInfo() throws Exception
    //    {
    //        restService.getRamQuotaInfo( CONTAINER_ID.toString() );
    //
    //        verify( containerHost ).getRamQuota();
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.getRamQuotaInfo( CONTAINER_ID.toString() );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testSetRamQuota() throws Exception
    //    {
    //        restService.setRamQuota( CONTAINER_ID.toString(), QUOTA );
    //
    //        verify( containerHost ).setRamQuota( QUOTA );
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.setRamQuota( CONTAINER_ID.toString(), QUOTA );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testGetCpuQuota() throws Exception
    //    {
    //        restService.getCpuQuota( CONTAINER_ID.toString() );
    //
    //        verify( containerHost ).getCpuQuota();
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.getCpuQuota( CONTAINER_ID.toString() );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testSetCpuQuota() throws Exception
    //    {
    //        restService.setCpuQuota( CONTAINER_ID.toString(), QUOTA );
    //
    //        verify( containerHost ).setCpuQuota( QUOTA );
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.setCpuQuota( CONTAINER_ID.toString(), QUOTA );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testGetCpuSet() throws Exception
    //    {
    //        restService.getCpuSet( CONTAINER_ID.toString() );
    //
    //        verify( containerHost ).getCpuSet();
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.getCpuSet( CONTAINER_ID.toString() );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testSetCpuSet() throws Exception
    //    {
    //        restService.setCpuSet( CONTAINER_ID.toString(), JSON );
    //
    //        verify( containerHost ).setCpuSet( anySet() );
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.setCpuSet( CONTAINER_ID.toString(), JSON );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testGetDiskQuota() throws Exception
    //    {
    //        restService.getDiskQuota( CONTAINER_ID.toString(), JSON );
    //
    //        verify( containerHost ).getDiskQuota( any( DiskPartition.class ) );
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.getDiskQuota( CONTAINER_ID.toString(), JSON );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testSetDiskQuota() throws Exception
    //    {
    //        restService.setDiskQuota( CONTAINER_ID.toString(), JSON );
    //
    //        verify( containerHost ).setDiskQuota( any( DiskQuota.class ) );
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        Response response1 = restService.setDiskQuota( CONTAINER_ID.toString(), JSON );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }
    //
    //
    //    @Test
    //    public void testSetDefaultGateway() throws Exception
    //    {
    //        //restService.setDefaultGateway( containerGateway );
    //
    //        //verify( peerManager ).setDefaultGateway( containerGateway );
    //
    //        doThrow( exception ).when( localPeer ).getContainerHostById( CONTAINER_ID );
    //
    //        //restService.setDefaultGateway( containerGateway );
    //    }
    //
    //
    //    @Test
    //    public void testGetContainerHostInfoById() throws Exception
    //    {
    //        restService.getContainerHostInfoById( CONTAINER_ID.toString() );
    //
    //        verify( localPeer ).getContainerHostInfoById( CONTAINER_ID );
    //
    //        doThrow( exception ).when( peerManager ).getLocalPeer();
    //
    //        Response response1 = restService.getContainerHostInfoById( CONTAINER_ID.toString() );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getState() );
    //    }


    //    @Test
    //    public void testGetReservedVnis() throws Exception
    //    {
    //        restService.getReservedVnis();
    //
    //        verify( peerManager ).getReservedVnis();
    //
    //        doThrow( exception ).when( peerManager ).getLocalPeer();
    //
    //        restService.getReservedVnis();
    //    }
    //
    //
    //    @Test
    //    public void testGetGateways() throws Exception
    //    {
    //        restService.getGateways();
    //
    //        verify( peerManager ).getGateways();
    //
    //        doThrow( exception ).when( peerManager ).getLocalPeer();
    //
    //        restService.getGateways();
    //    }
    //
    //
    //    @Test
    //    public void testReserveVni() throws Exception
    //    {
    //        restService.reserveVni( vni );
    //
    //        verify( peerManager ).reserveVni( any( Vni.class ) );
    //
    //        doThrow( exception ).when( peerManager ).getLocalPeer();
    //
    //        restService.reserveVni( vni );
    //
    //    }
}
