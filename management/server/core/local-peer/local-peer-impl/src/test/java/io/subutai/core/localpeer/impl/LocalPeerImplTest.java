package io.subutai.core.localpeer.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.openjpa.persistence.EntityManagerFactoryImpl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RequestListener;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.settings.Common;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.localpeer.impl.dao.ResourceHostDataService;
import io.subutai.core.localpeer.impl.entity.ContainerHostEntity;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.object.relation.api.model.Relation;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;
import io.subutai.core.object.relation.api.model.RelationMeta;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.strategy.api.StrategyManager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class LocalPeerImplTest
{
    private static final String PEER_ID = UUID.randomUUID().toString();
    private static final String ENVIRONMENT_ID = UUID.randomUUID().toString();
    private static final String LOCAL_PEER_ID = UUID.randomUUID().toString();
    private static final String OWNER_ID = UUID.randomUUID().toString();
    private static final String LOCAL_PEER_NAME = "local peer";
    private static final String MANAGEMENT_HOST_ID = UUID.randomUUID().toString();
    private static final String RESOURCE_HOST_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_HOST_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_HOST_NAME = "container_host_name";
    private static final String RESOURCE_HOST_NAME = "foo";
    private static final String TEMPLATE_NAME = "master";
    private static final String IP = "127.0.0.1";
    private static final Object REQUEST = new Object();
    private static final Object RESPONSE = new Object();
    private static final String RECIPIENT = "recipient";
    private static final int PID = 123;
    private static final int QUOTA = 123;
    private static final String P2P_IP = "10.11.0.1";
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String ENV_ID = UUID.randomUUID().toString();
    private static final String HOSTNAME = "hostname";
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String INTERFACE_NAME = "eth0";

    @Mock
    NetworkManager networkManager;


    @Mock
    PeerManager peerManager;
    @Mock
    TemplateManager templateRegistry;
    @Mock
    ResourceHost managementHost;
    @Mock
    CommandExecutor commandExecutor;
    @Mock
    StrategyManager strategyManager;
    @Mock
    QuotaManager quotaManager;
    @Mock
    Monitor monitor;
    @Mock
    IdentityManager identityManager;
    @Mock
    HostRegistry hostRegistry;
    @Mock
    RequestListener requestListener;
    @Mock
    RelationManager relationManager;
    @Mock
    User user;
    @Mock
    Relation relation;

    @Mock
    ResourceHostDataService resourceHostDataService;
    @Mock
    ResourceHostEntity resourceHost;
    @Mock
    CommandUtil commandUtil;
    @Mock
    ExceptionUtil exceptionUtil;
    @Mock
    PeerInfo peerInfo;
    @Mock
    ContainerId containerId;
    @Mock
    PeerId peerId;
    @Mock
    ContainerHostEntity containerHost;
    @Mock
    ContainerHostInfo containerHostInfo;
    @Mock
    TemplateKurjun template;
    @Mock
    ContainerHostInfoModel containerHostInfoModel;

    @Mock
    CommandException commandException;
    @Mock
    HostInfo hostInfo;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    CommandCallback commandCallback;
    @Mock
    ResourceHostInfo resourceHostInfo;
    @Mock
    DaoManager daoManager;

    @Mock
    SecurityManager securityManager;
    @Mock
    KeyManager keyManager;

    LocalPeerImpl localPeer;

    Map<String, String> peerMap = new HashMap<>();

    @Mock
    EntityManagerFactoryImpl entityManagerFactory;

    @Mock
    private ByteValueResource cpuQuota;
    @Mock
    private EnvironmentId environmentId;

    @Mock
    private QuotaAlertValue quotaAlertValue;

    @Mock
    private ServiceLocator serviceLocator;

    @Mock
    private HostInterfaces hostInterfaces;

    @Mock
    private HostInterfaceModel anHostInterface;

    @Mock
    private ExecutorService singleThreadExecutorService;

    @Mock
    Callable callable;

    @Mock
    Future future;


    @Before
    public void setUp() throws Exception
    {
        when( peerInfo.getId() ).thenReturn( LOCAL_PEER_ID );
        when( peerInfo.getName() ).thenReturn( LOCAL_PEER_NAME );
        when( peerInfo.getOwnerId() ).thenReturn( OWNER_ID );
        when( peerInfo.getIp() ).thenReturn( IP );

        when( anHostInterface.getName() ).thenReturn( INTERFACE_NAME );
        when( anHostInterface.getIp() ).thenReturn( IP );

        peerMap = new HashMap<>();
        peerMap.put( IP, P2P_IP );
        localPeer = spy( new LocalPeerImpl( daoManager, templateRegistry, quotaManager, commandExecutor, hostRegistry,
                monitor, securityManager ) );
        localPeer.setIdentityManager( identityManager );
        localPeer.setRelationManager( relationManager );

        localPeer.peerInfo = peerInfo;
        localPeer.resourceHostDataService = resourceHostDataService;
        localPeer.resourceHosts = Sets.newHashSet( ( ResourceHost ) resourceHost );
        localPeer.commandUtil = commandUtil;
        localPeer.exceptionUtil = exceptionUtil;
        localPeer.managementHost = managementHost;
        localPeer.requestListeners = Sets.newHashSet( requestListener );

        when( daoManager.getEntityManagerFactory() ).thenReturn( entityManagerFactory );
        when( managementHost.getId() ).thenReturn( MANAGEMENT_HOST_ID );
        when( managementHost.getHostInterfaces() ).thenReturn( hostInterfaces );
        when( managementHost.getInterfaceByName( INTERFACE_NAME ) ).thenReturn( anHostInterface );

        when( resourceHost.getId() ).thenReturn( RESOURCE_HOST_ID );
        when( containerHost.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerHost.getContainerId() ).thenReturn( containerId );

        when( peerId.getId() ).thenReturn( PEER_ID );
        when( containerId.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerId.getPeerId() ).thenReturn( peerId );
        when( resourceHost.getContainerHostById( CONTAINER_HOST_ID ) ).thenReturn( containerHost );
        when( resourceHost.getHostname() ).thenReturn( RESOURCE_HOST_NAME );
        when( securityManager.getKeyManager() ).thenReturn( keyManager );
        when( resourceHostDataService.getAll() ).thenReturn( Sets.newHashSet( resourceHost ) );
        when( templateRegistry.getTemplate( TEMPLATE_NAME ) ).thenReturn( template );
        when( template.getName() ).thenReturn( TEMPLATE_NAME );
        when( resourceHost.isConnected() ).thenReturn( true );
        when( hostRegistry.getContainerHostInfoById( CONTAINER_HOST_ID ) ).thenReturn( containerHostInfo );
        when( hostRegistry.getHostInfoById( CONTAINER_HOST_ID ) ).thenReturn( containerHostInfo );
        when( containerHostInfo.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerHost.getHostname() ).thenReturn( CONTAINER_HOST_NAME );
        when( environmentId.getId() ).thenReturn( ENVIRONMENT_ID );
        when( containerHost.getEnvironmentId() ).thenReturn( environmentId );
        doReturn( resourceHost ).when( localPeer ).getResourceHostByName( RESOURCE_HOST_NAME );
        doReturn( containerHost ).when( resourceHost ).getContainerHostByName( CONTAINER_HOST_NAME );
        when( containerHost.getParent() ).thenReturn( resourceHost );
        when( containerHostInfo.getState() ).thenReturn( ContainerHostState.RUNNING );
        when( containerHost.isConnected() ).thenReturn( true );
        when( resourceHost.getContainerHosts() ).thenReturn( Sets.<ContainerHost>newHashSet( containerHost ) );
        when( requestListener.getRecipient() ).thenReturn( RECIPIENT );
        doReturn( RESPONSE ).when( requestListener ).onRequest( any( Payload.class ) );

        peerMap = new HashMap<>();
        peerMap.put( IP, P2P_IP );
        when( environmentId.getId() ).thenReturn( ENV_ID );
        when( hostInfo.getId() ).thenReturn( HOST_ID );
        when( hostInfo.getHostname() ).thenReturn( HOSTNAME );
        when( hostInfo.getArch() ).thenReturn( ARCH );
        when( hostInterfaces.getAll() ).thenReturn( Sets.newHashSet( anHostInterface ) );
        when( hostInfo.getHostInterfaces() ).thenReturn( hostInterfaces );

        localPeer.serviceLocator = serviceLocator;
        when( singleThreadExecutorService.submit( any( Callable.class ) ) ).thenReturn( future );
        when( serviceLocator.getService( NetworkManager.class ) ).thenReturn( networkManager );

        when( identityManager.getUserByKeyId( anyString() ) ).thenReturn( user );
        when( relationManager.buildRelation( any( RelationInfoMeta.class ), any( RelationMeta.class ) ) )
                .thenReturn( relation );
    }


    @Test
    public void testGetResourceHostDataService() throws Exception
    {
        assertNotNull( localPeer.createResourceHostDataService() );
    }


    @Test
    public void testDispose() throws Exception
    {
        localPeer.dispose();

        verify( resourceHost ).dispose();
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals( LOCAL_PEER_ID, localPeer.getId() );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertEquals( LOCAL_PEER_NAME, localPeer.getName() );
    }


    @Test
    public void testGetOwnerId() throws Exception
    {
        assertEquals( OWNER_ID, localPeer.getOwnerId() );
    }


    @Test
    @Ignore
    public void testGetContainerHostState() throws Exception
    {
        localPeer.getContainerState( containerHost.getContainerId() );

        verify( containerHost ).getState();
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetContainerHostByName() throws Exception
    {
        assertEquals( containerHost, localPeer.getContainerHostByName( CONTAINER_HOST_NAME ) );

        doThrow( new HostNotFoundException( "" ) ).when( resourceHost ).getContainerHostByName( CONTAINER_HOST_NAME );

        localPeer.getContainerHostByName( CONTAINER_HOST_NAME );
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetContainerHostById() throws Exception
    {
        assertEquals( containerHost, localPeer.getContainerHostById( CONTAINER_HOST_ID ) );

        doThrow( new HostNotFoundException( "" ) ).when( resourceHost ).getContainerHostById( CONTAINER_HOST_ID );

        localPeer.getContainerHostById( CONTAINER_HOST_ID );
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetResourceHostByName() throws Exception
    {
        assertEquals( resourceHost, localPeer.getResourceHostByName( RESOURCE_HOST_NAME ) );

        localPeer.getResourceHostByName( "DUMMY NAME" );
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetResourceHostById() throws Exception
    {
        assertEquals( resourceHost, localPeer.getResourceHostById( RESOURCE_HOST_ID ) );

        localPeer.getResourceHostById( UUID.randomUUID().toString() );
    }


    @Test
    public void testGetResourceHostByContainerName() throws Exception
    {
        assertEquals( resourceHost, localPeer.getResourceHostByContainerName( CONTAINER_HOST_NAME ) );
    }


    @Test
    public void testGetResourceHostByContainerId() throws Exception
    {
        assertEquals( resourceHost, localPeer.getResourceHostByContainerId( CONTAINER_HOST_ID ) );
    }


    @Test
    public void testBindHost() throws Exception
    {

        assertEquals( resourceHost, localPeer.bindHost( RESOURCE_HOST_ID ) );

        assertEquals( containerHost, localPeer.bindHost( CONTAINER_HOST_ID ) );
    }


    @Test( expected = PeerException.class )
    public void testStartContainer() throws Exception
    {
        localPeer.startContainer( containerHost.getContainerId() );

        verify( resourceHost ).startContainerHost( containerHost );

        RuntimeException cause = mock( RuntimeException.class );

        doThrow( cause ).when( resourceHost ).startContainerHost( containerHost );

        localPeer.startContainer( containerHost.getContainerId() );
    }


    @Test( expected = PeerException.class )
    public void testStopContainer() throws Exception
    {
        localPeer.stopContainer( containerHost.getContainerId() );

        verify( resourceHost ).stopContainerHost( containerHost );

        RuntimeException cause = mock( RuntimeException.class );

        doThrow( cause ).when( resourceHost ).stopContainerHost( containerHost );

        localPeer.stopContainer( containerHost.getContainerId() );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {

        doThrow( new ResourceHostException( "" ) ).when( resourceHost ).destroyContainerHost( containerHost );

        try
        {
            localPeer.destroyContainer( containerHost.getContainerId() );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    private void throwCommandException() throws CommandException
    {
        doThrow( commandException ).when( commandUtil ).execute( any( RequestBuilder.class ), any( Host.class ) );
    }


    @Ignore
    @Test( expected = PeerException.class )
    public void testSetDefaultGateway() throws Exception
    {

        verify( commandUtil ).execute( any( RequestBuilder.class ), eq( containerHost ) );

        throwCommandException();
    }


    @Test
    @Ignore
    public void testIsConnected() throws Exception
    {
        assertTrue( localPeer.isConnected( containerHost.getContainerId() ) );

        when( hostRegistry.getHostInfoById( CONTAINER_HOST_ID ) ).thenReturn( hostInfo );

        assertTrue( localPeer.isConnected( containerHost.getContainerId() ) );

        HostDisconnectedException hostDisconnectedException = mock( HostDisconnectedException.class );

        doThrow( hostDisconnectedException ).when( hostRegistry ).getHostInfoById( CONTAINER_HOST_ID );

        assertFalse( localPeer.isConnected( containerHost.getContainerId() ) );
    }


    @Test( expected = PeerException.class )
    @Ignore
    public void testGetQuotaInfo() throws Exception
    {
        localPeer.getQuota( containerId );

        verify( quotaManager ).getQuota( containerId );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).getQuota( containerId );

        localPeer.getQuota( containerId );
    }


    @Test( expected = PeerException.class )
    public void testSetQuota() throws Exception
    {
        ContainerQuota quotaInfo = mock( ContainerQuota.class );

        localPeer.setQuota( containerId, quotaInfo );

        verify( quotaManager ).setQuota( containerId, quotaInfo );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).setQuota( containerId, quotaInfo );

        localPeer.setQuota( containerId, quotaInfo );
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetManagementHost() throws Exception
    {
        assertEquals( managementHost, localPeer.getManagementHost() );

        doThrow( new HostNotFoundException( "" )).when( localPeer ).getResourceHostByContainerName( Common.MANAGEMENT_HOSTNAME );

        localPeer.managementHost = null;

        localPeer.getManagementHost();
    }


    @Test
    public void testAddResourceHost() throws Exception
    {
        localPeer.resourceHosts.clear();

        localPeer.addResourceHost( resourceHost );

        assertTrue( localPeer.getResourceHosts().contains( resourceHost ) );
    }


    @Test
    public void testExecute() throws Exception
    {
        localPeer.execute( requestBuilder, containerHost, commandCallback );

        verify( commandExecutor ).execute( CONTAINER_HOST_ID, requestBuilder, commandCallback );

        localPeer.execute( requestBuilder, containerHost );

        verify( commandExecutor ).execute( CONTAINER_HOST_ID, requestBuilder );
    }


    @Test
    public void testExecuteAsync() throws Exception
    {
        localPeer.executeAsync( requestBuilder, containerHost, commandCallback );

        verify( commandExecutor ).executeAsync( CONTAINER_HOST_ID, requestBuilder, commandCallback );

        localPeer.executeAsync( requestBuilder, containerHost );

        verify( commandExecutor ).executeAsync( CONTAINER_HOST_ID, requestBuilder );
    }


    @Test
    public void testIsLocal() throws Exception
    {
        assertTrue( localPeer.isLocal() );
    }


    @Test
    public void testIsOnline() throws Exception
    {
        assertTrue( localPeer.isOnline() );
    }


    @Test
    public void testGetTemplate() throws Exception
    {
        localPeer.getTemplate( TEMPLATE_NAME );

        verify( templateRegistry ).getTemplate( TEMPLATE_NAME );
    }


    @Test( expected = PeerException.class )
    public void testSendRequestInternal() throws Exception
    {
        localPeer.sendRequestInternal( REQUEST, RECIPIENT, Object.class );

        verify( requestListener ).onRequest( any( Payload.class ) );

        Map<String, String> headers = Maps.newHashMap();

        localPeer.sendRequest( REQUEST, RECIPIENT, 1, headers );

        verify( requestListener, times( 2 ) ).onRequest( any( Payload.class ) );

        localPeer.sendRequest( REQUEST, RECIPIENT, 1, Object.class, 1, headers );

        verify( requestListener, times( 3 ) ).onRequest( any( Payload.class ) );

        doThrow( new RuntimeException() ).when( requestListener ).onRequest( any( Payload.class ) );

        localPeer.sendRequestInternal( REQUEST, RECIPIENT, Object.class );
    }


    @Test
    public void testOnHeartbeat() throws Exception
    {
        when( resourceHostInfo.getHostname() ).thenReturn( Common.MANAGEMENT_HOSTNAME );
        when( resourceHostInfo.getId() ).thenReturn( MANAGEMENT_HOST_ID );
        when( resourceHostInfo.getHostInterfaces() ).thenReturn( hostInterfaces );

        localPeer.initialized = true;
        localPeer.onHeartbeat( resourceHostInfo, Sets.newHashSet( quotaAlertValue ) );

        localPeer.managementHost = null;

        localPeer.onHeartbeat( resourceHostInfo, Sets.newHashSet( quotaAlertValue ) );

        when( resourceHostInfo.getHostname() ).thenReturn( RESOURCE_HOST_NAME );
        when( resourceHostInfo.getId() ).thenReturn( RESOURCE_HOST_ID );

        localPeer.onHeartbeat( resourceHostInfo, Sets.newHashSet( quotaAlertValue ) );

        verify( resourceHost ).updateHostInfo( resourceHostInfo );

        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).getResourceHostById( anyString() );
    }


    @Test
    public void testSaveResourceHostContainers() throws Exception
    {

        ContainerHostInfo containerHostInfo1 = mock( ContainerHostInfo.class );
        when( containerHostInfo1.getId() ).thenReturn( UUID.randomUUID().toString() );

        when( resourceHostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo1 ) );


        resourceHost.updateHostInfo( resourceHostInfo );


        when( resourceHostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo ) );


        resourceHost.updateHostInfo( resourceHostInfo );
    }


    @Test( expected = PeerException.class )
    public void testGetProcessResourceUsage() throws Exception
    {
        localPeer.getProcessResourceUsage( containerHost.getContainerId(), PID );

        verify( monitor ).getProcessResourceUsage( containerHost.getContainerId(), PID );

        doThrow( new MonitorException( "" ) ).when( monitor ).getProcessResourceUsage( containerId, PID );

        localPeer.getProcessResourceUsage( containerHost.getContainerId(), PID );
    }


    @Test( expected = PeerException.class )
    public void testGetRamQuota() throws Exception
    {
        localPeer.getQuota( containerId );

        verify( quotaManager ).getQuota( containerId );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).getQuota( containerId );

        localPeer.getQuota( containerId );
    }


    @Test( expected = PeerException.class )
    public void testGetCpuSet() throws Exception
    {
        localPeer.getCpuSet( containerHost );

        verify( quotaManager ).getCpuSet( containerId );

        doThrow( new QuotaException() ).when( quotaManager ).getCpuSet( containerId );

        localPeer.getCpuSet( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testSetCpuSet() throws Exception
    {
        localPeer.setCpuSet( containerHost, Sets.newHashSet( QUOTA ) );

        verify( quotaManager ).setCpuSet( eq( containerId ), anySet() );

        doThrow( new QuotaException() ).when( quotaManager ).setCpuSet( eq( containerId ), anySet() );

        localPeer.setCpuSet( containerHost, Sets.newHashSet( QUOTA ) );
    }
}
