package io.subutai.core.peer.impl;


import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.exception.HTTPException;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.CpuQuotaInfo;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.QuotaInfo;
import io.subutai.common.quota.QuotaType;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageException;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.impl.command.BlockingCommandCallback;
import io.subutai.core.peer.impl.command.CommandResponseListener;
import io.subutai.core.peer.impl.request.MessageRequest;
import io.subutai.core.peer.impl.request.MessageResponse;
import io.subutai.core.peer.impl.request.MessageResponseListener;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
@Ignore
public class RemotePeerImplTest
{
    private static final String PATH = "path";
    private static final String ALIAS = "alias";
    private static final String PARAM_NAME = "param";
    private static final String PARAM_VALUE = "param value";
    private static final String HEADER_NAME = "header";
    private static final String HEADER_VALUE = "header value";
    private static final String TEMPLATE_NAME = "master";
    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static final String ENV_ID = UUID.randomUUID().toString();
    private static final String PEER_ID = UUID.randomUUID().toString();
    private static final String IP = "127.0.0.1";
    private static final int PID = 123;
    private static final int VLAN = 123;
    private static final int QUOTA = 123;
    private static final int PERCENT = 75;
    private static final Set<Integer> CPU_SET = Sets.newHashSet( 1, 2, 3 );
    private static final UUID MESSAGE_ID = UUID.randomUUID();
    private static final int TIMEOUT = 100;
    private static final String RECIPIENT = "recipient";
    private static final Object REQUEST = new Object();
    private static final String SUBNET = "192.168.1.0/24";
    private static final String RESPONSE = "RESPONSE";
    private static final String N2N_IP = "10.11.0.1";
    @Mock
    LocalPeer localPeer;
    @Mock
    PeerInfo peerInfo;
    @Mock
    Messenger messenger;
    @Mock
    CommandResponseListener commandResponseListener;
    @Mock
    MessageResponseListener messageResponseListener;
    @Mock
    RestUtil restUtil;
    @Mock
    JsonUtil jsonUtil;
    @Mock
    HTTPException httpException;
    @Mock
    EnvironmentContainerHost containerHost;
    @Mock
    CommandCallback commandCallback;
    @Mock
    Semaphore semaphore;
    @Mock
    Message message;
    @Mock
    MessageException messageException;

    Map<String, String> params;
    Map<String, String> headers;

    RequestBuilder requestBuilder;

    RemotePeerImpl remotePeer;

    Map<String, String> peerMap = new HashMap<>();
    @Mock
    private WebClient webClient;

    @Mock
    ContainerId containerId;

    @Mock
    private EnvironmentId environmentId;


    @Before
    public void setUp() throws Exception
    {
        peerMap = new HashMap<>();
        peerMap.put( IP, N2N_IP );
        requestBuilder = new RequestBuilder( "pwd" );
        params = Maps.newHashMap();
        params.put( PARAM_NAME, PARAM_VALUE );
        headers = Maps.newHashMap();
        headers.put( HEADER_NAME, HEADER_VALUE );
        remotePeer = spy( new RemotePeerImpl( localPeer, peerInfo, messenger, commandResponseListener,
                messageResponseListener, null ) );
        remotePeer.restUtil = restUtil;
        remotePeer.jsonUtil = jsonUtil;

        when( restUtil.createTrustedWebClientWithAuthAndProviders( anyString(), anyString(), any() ) )
                .thenReturn( webClient );
        when( containerHost.getId() ).thenReturn( CONTAINER_ID );
        when( containerHost.getContainerId() ).thenReturn( containerId );
        when( containerId.getId() ).thenReturn( CONTAINER_ID );
        when( environmentId.getId() ).thenReturn( ENV_ID );
        when( containerId.getEnvironmentId() ).thenReturn( environmentId );
        when( localPeer.getId() ).thenReturn( PEER_ID );
        when( peerInfo.getId() ).thenReturn( PEER_ID );
        when( remotePeer.getId() ).thenReturn( PEER_ID );
        //        when( remotePeer.getPeerInfo() ).thenReturn( new PeerInfo() );
        when( containerHost.isConnected() ).thenReturn( true );
        when( containerHost.getEnvironmentId() ).thenReturn( ENV_ID );
        when( messenger.createMessage( anyObject() ) ).thenReturn( message );
        when( message.getId() ).thenReturn( MESSAGE_ID );
    }


    private void throwException() throws HTTPException
    {
        doThrow( httpException ).when( restUtil )
                                .request( any( RestUtil.RequestType.class ), anyString(), anyString(), anyMap(),
                                        anyMap(), any() );
    }


    private void throwWebClientException() throws HTTPException
    {
        doThrow( httpException ).when( webClient ).post( anyObject(), any( Class.class ) );
    }


    @Test
    public void testRequest() throws Exception
    {
        remotePeer.request( RestUtil.RequestType.GET, PATH, ALIAS, params, headers );

        verify( restUtil )
                .request( eq( RestUtil.RequestType.GET ), anyString(), eq( ALIAS ), eq( params ), eq( headers ),
                        any() );
    }


    @Test
    public void testGet() throws Exception
    {

        remotePeer.get( PATH, ALIAS, params, headers );

        verify( restUtil )
                .request( eq( RestUtil.RequestType.GET ), anyString(), eq( ALIAS ), eq( params ), eq( headers ),
                        any() );
    }


    @Test
    public void testPost() throws Exception
    {
        remotePeer.post( PATH, ALIAS, params, headers );

        verify( restUtil )
                .request( eq( RestUtil.RequestType.POST ), anyString(), eq( ALIAS ), eq( params ), eq( headers ),
                        any() );
    }


    @Test
    public void testDelete() throws Exception
    {
        remotePeer.delete( PATH, ALIAS, params, headers );

        verify( restUtil )
                .request( eq( RestUtil.RequestType.DELETE ), anyString(), eq( ALIAS ), eq( params ), eq( headers ),
                        any() );
    }


    @Test
    public void testGetId() throws Exception
    {
        remotePeer.getId();

        verify( peerInfo ).getId();
    }


    //    @Test( expected = PeerException.class )
    //    public void testGetRemoteId() throws Exception
    //    {
    //        String ID = UUID.randomUUID().toString();
    //        when( restUtil.request( eq( RestUtil.RequestType.GET ), anyString(), anyString(), anyMap(), anyMap() ) )
    //                .thenReturn( ID );
    //
    //        PeerInfo id = remotePeer.getPeerInfo();
    //
    //        assertEquals( ID, id.toString() );
    //
    //        throwException();
    //
    //        //        remotePeer.getRemoteId();
    //    }


    //    @Test( expected = PeerException.class )
    //    public void testIsOnline() throws Exception
    //    {
    //        UUID ID = UUID.randomUUID();
    //        when( restUtil.request( eq( RestUtil.RequestType.GET ), anyString(), anyString(), anyMap(), anyMap() ) )
    //                .thenReturn( ID.toString() );
    //        when( peerInfo.getId() ).thenReturn( ID.toString() );
    //
    //        assertTrue( remotePeer.isOnline() );
    //
    //        throwException();
    //
    //        remotePeer.isOnline();
    //    }


    @Test
    public void testIsLocal() throws Exception
    {
        assertFalse( remotePeer.isLocal() );
    }


    @Test
    public void testGetName() throws Exception
    {
        remotePeer.getName();

        verify( peerInfo ).getName();
    }


    @Test
    public void testGetOwnerId() throws Exception
    {
        remotePeer.getOwnerId();

        verify( peerInfo ).getOwnerId();
    }


    //    @Test
    //    public void testGetPeerInfo() throws Exception
    //    {
    //        assertEquals( peerInfo, remotePeer.getPeerInfo() );
    //    }


    @Test( expected = PeerException.class )
    public void testGetTemplate() throws Exception
    {
        remotePeer.getTemplate( TEMPLATE_NAME );

        verify( jsonUtil ).from( anyString(), eq( Template.class ) );

        throwException();

        remotePeer.getTemplate( TEMPLATE_NAME );
    }


    @Test( expected = PeerException.class )
    public void testStartContainer() throws Exception
    {
        remotePeer.startContainer( containerHost.getContainerId() );

        //verify( localPeer ).getId();

        throwWebClientException();

        remotePeer.startContainer( containerHost.getContainerId() );
    }


    @Test( expected = PeerException.class )
    public void testStopContainer() throws Exception
    {
        remotePeer.stopContainer( containerHost.getContainerId() );

        //verify( localPeer ).getId();

        throwWebClientException();

        remotePeer.stopContainer( containerHost.getContainerId() );
    }


    @Test( expected = PeerException.class )
    public void testDestroyContainer() throws Exception
    {
        remotePeer.destroyContainer( containerHost.getContainerId() );

        //verify( localPeer ).getId();

        throwWebClientException();

        remotePeer.destroyContainer( containerHost.getContainerId() );
    }


    @Test( expected = PeerException.class )
    public void testSetDefaultGateway() throws Exception
    {
        remotePeer.setDefaultGateway( containerHost, IP );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.setDefaultGateway( containerHost, IP );
    }


//    @Test
//    public void testIsConnected() throws Exception
//    {
//        assertTrue( remotePeer.isConnected( containerHost.getContainerId() ) );
//
//        throwException();
//
//        //assertFalse( remotePeer.isConnected( containerHost ) );
//    }


    @Test( expected = PeerException.class )
    public void testGetProcessResourceUsage() throws Exception
    {
        remotePeer.getProcessResourceUsage( containerHost.getContainerId(), PID );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getProcessResourceUsage( containerHost.getContainerId(), PID );
    }


    @Test( expected = PeerException.class )
    public void testGetContainerHostState() throws Exception
    {
        remotePeer.getContainerState( containerHost.getContainerId() );

        //verify( localPeer ).getId();

        throwWebClientException();

        remotePeer.getContainerState( containerHost.getContainerId() );
    }


    @Test( expected = PeerException.class )
    public void testGetRamQuota() throws Exception
    {
        when( jsonUtil.from( anyString(), eq( Integer.class ) ) ).thenReturn( QUOTA );

        remotePeer.getRamQuota( containerHost );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getRamQuota( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testGetRamQuotaInfo() throws Exception
    {
        remotePeer.getRamQuotaInfo( containerHost );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getRamQuotaInfo( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testSetRamQuota() throws Exception
    {
        remotePeer.setRamQuota( containerHost, QUOTA );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.setRamQuota( containerHost, QUOTA );
    }


    @Test( expected = PeerException.class )
    public void testGetCpuQuota() throws Exception
    {
        when( jsonUtil.from( anyString(), eq( Integer.class ) ) ).thenReturn( QUOTA );

        remotePeer.getCpuQuota( containerHost );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getCpuQuota( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testGetCpuQuotaInfo() throws Exception
    {
        CpuQuotaInfo cpuQuotaInfo = mock( CpuQuotaInfo.class );
        when( jsonUtil.from( anyString(), eq( CpuQuotaInfo.class ) ) ).thenReturn( cpuQuotaInfo );

        remotePeer.getCpuQuotaInfo( containerHost );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getCpuQuotaInfo( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testSetCpuQuota() throws Exception
    {
        remotePeer.setCpuQuota( containerHost, PERCENT );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.setCpuQuota( containerHost, PERCENT );
    }


    @Test( expected = PeerException.class )
    public void testGetCpuSet() throws Exception
    {
        when( jsonUtil.from( anyString(), any( Type.class ) ) ).thenReturn( CPU_SET );

        remotePeer.getCpuSet( containerHost );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getCpuSet( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testSetCpuSet() throws Exception
    {
        remotePeer.setCpuSet( containerHost, CPU_SET );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.setCpuSet( containerHost, CPU_SET );
    }


    @Test( expected = PeerException.class )
    public void testGetDiskQuota() throws Exception
    {
        DiskQuota diskQuota = mock( DiskQuota.class );
        when( jsonUtil.from( anyString(), any( Type.class ) ) ).thenReturn( diskQuota );

        remotePeer.getDiskQuota( containerHost, DiskPartition.VAR );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getDiskQuota( containerHost, DiskPartition.VAR );
    }


    @Test( expected = PeerException.class )
    public void testSetDiskQuota() throws Exception
    {
        DiskQuota diskQuota = mock( DiskQuota.class );

        remotePeer.setDiskQuota( containerHost, diskQuota );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.setDiskQuota( containerHost, diskQuota );
    }


    @Test( expected = PeerException.class )
    public void testSetRamQuota2() throws Exception
    {
        RamQuota ramQuota = mock( RamQuota.class );

        remotePeer.setRamQuota( containerHost, ramQuota );

        //verify( peerInfo ).getId();

        throwException();

        remotePeer.setRamQuota( containerHost, ramQuota );
    }


    @Test( expected = PeerException.class )
    public void testGetAvailableRamQuota() throws Exception
    {
        when( jsonUtil.from( anyString(), eq( Integer.class ) ) ).thenReturn( QUOTA );

        remotePeer.getAvailableRamQuota( containerHost );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getAvailableRamQuota( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testGetAvailableCpuQuota() throws Exception
    {
        when( jsonUtil.from( anyString(), eq( Integer.class ) ) ).thenReturn( QUOTA );

        remotePeer.getAvailableCpuQuota( containerHost );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getAvailableCpuQuota( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testGetAvailableDiskQuota() throws Exception
    {
        remotePeer.getAvailableDiskQuota( containerHost, DiskPartition.VAR );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getAvailableDiskQuota( containerHost, DiskPartition.VAR );
    }


    @Test( expected = PeerException.class )
    public void testGetQuotaInfo() throws Exception
    {
        remotePeer.getQuotaInfo( containerHost, QuotaType.QUOTA_TYPE_CPU );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.getQuotaInfo( containerHost, QuotaType.QUOTA_TYPE_CPU );
    }


    @Test( expected = PeerException.class )
    public void testSetQuota() throws Exception
    {
        QuotaInfo quotaInfo = mock( QuotaInfo.class );

        remotePeer.setQuota( containerHost, quotaInfo );

        //verify( localPeer ).getId();

        throwException();

        remotePeer.setQuota( containerHost, quotaInfo );
    }


    @Test
    public void testExecute() throws Exception
    {
        BlockingCommandCallback blockingCommandCallback = mock( BlockingCommandCallback.class );
        when( remotePeer.getBlockingCommandCallback( any( CommandCallback.class ) ) )
                .thenReturn( blockingCommandCallback );

        remotePeer.execute( requestBuilder, containerHost );

        verify( blockingCommandCallback ).getCommandResult();

        remotePeer.execute( requestBuilder, containerHost, commandCallback );

        verify( blockingCommandCallback, times( 2 ) ).getCommandResult();
    }


    @Test
    public void testExecuteAsync() throws Exception
    {
        remotePeer.executeAsync( requestBuilder, containerHost );

        verify( commandResponseListener )
                .addCallback( any( UUID.class ), any( CommandCallback.class ), anyInt(), any( Semaphore.class ) );

        remotePeer.executeAsync( requestBuilder, containerHost, commandCallback );

        verify( commandResponseListener )
                .addCallback( any( UUID.class ), eq( commandCallback ), anyInt(), any( Semaphore.class ) );

        remotePeer.executeAsync( requestBuilder, containerHost, commandCallback, semaphore );

        verify( commandResponseListener )
                .addCallback( any( UUID.class ), eq( commandCallback ), anyInt(), eq( semaphore ) );

        doThrow( messageException ).when( messenger )
                                   .sendMessage( any( Peer.class ), any( Message.class ), anyString(), anyInt(),
                                           anyMap() );

        try
        {
            remotePeer.executeAsync( requestBuilder, containerHost, commandCallback, semaphore );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }

        try
        {
            Host host = mock( Host.class );
            remotePeer.executeAsync( requestBuilder, host, commandCallback, semaphore );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }
        try
        {
            when( containerHost.isConnected() ).thenReturn( false );
            remotePeer.executeAsync( requestBuilder, containerHost, commandCallback, semaphore );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }
    }


    @Test( expected = PeerException.class )
    public void testSendRequestInternal() throws Exception
    {
        remotePeer.sendRequestInternal( REQUEST, RECIPIENT, TIMEOUT, headers );

        verify( messenger ).sendMessage( any( Peer.class ), eq( message ), anyString(), eq( TIMEOUT ), eq( headers ) );


        doThrow( messageException ).when( messenger )
                                   .sendMessage( any( Peer.class ), any( Message.class ), anyString(), anyInt(),
                                           anyMap() );

        remotePeer.sendRequestInternal( REQUEST, RECIPIENT, TIMEOUT, headers );
    }


    @Test
    public void testSendRequest() throws Exception
    {

        remotePeer.sendRequest( REQUEST, RECIPIENT, TIMEOUT, headers );

        verify( messenger ).sendMessage( any( Peer.class ), eq( message ), anyString(), eq( TIMEOUT ), eq( headers ) );

        MessageResponse messageResponse = mock( MessageResponse.class );
        when( messageResponseListener.waitResponse( any( MessageRequest.class ), anyInt(), anyInt() ) )
                .thenReturn( messageResponse );

        remotePeer.sendRequest( REQUEST, RECIPIENT, TIMEOUT, Object.class, TIMEOUT, headers );

        verify( messageResponse ).getException();

        Payload payload = mock( Payload.class );
        when( messageResponse.getPayload() ).thenReturn( payload );


        remotePeer.sendRequest( REQUEST, RECIPIENT, TIMEOUT, Object.class, TIMEOUT, headers );

        verify( payload ).getMessage( any( Class.class ) );

        when( messageResponse.getException() ).thenReturn( "exception" );

        try
        {
            remotePeer.sendRequest( REQUEST, RECIPIENT, TIMEOUT, Object.class, TIMEOUT, headers );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test( expected = PeerException.class )
    public void testReserveVni() throws Exception
    {
        Vni vni = mock( Vni.class );
        when( vni.getEnvironmentId() ).thenReturn( ENV_ID );
        when( restUtil.request( eq( RestUtil.RequestType.POST ), anyString(), anyString(), anyMap(), anyMap() ) )
                .thenReturn( String.valueOf( VLAN ) );


        assertEquals( VLAN, remotePeer.reserveVni( vni ) );

        throwException();

        remotePeer.reserveVni( vni );
    }


    @Test( expected = PeerException.class )
    public void testGetContainerHostInfoById() throws Exception
    {
        remotePeer.getContainerHostInfoById( CONTAINER_ID );

        verify( restUtil ).request( eq( RestUtil.RequestType.GET ), anyString(), anyString(), anyMap(), anyMap() );

        throwException();

        remotePeer.getContainerHostInfoById( CONTAINER_ID );
    }


    @Test( expected = PeerException.class )
    public void testGetGateways() throws Exception
    {
        remotePeer.getGateways();

        verify( restUtil )
                .request( eq( RestUtil.RequestType.GET ), anyString(), anyString(), anyMap(), anyMap(), any() );

        throwException();

        remotePeer.getGateways();
    }


    @Test( expected = PeerException.class )
    public void testGetReservedVnis() throws Exception
    {
        remotePeer.getReservedVnis();

        verify( restUtil )
                .request( eq( RestUtil.RequestType.GET ), anyString(), anyString(), anyMap(), anyMap(), any() );

        throwException();

        remotePeer.getReservedVnis();
    }
}
