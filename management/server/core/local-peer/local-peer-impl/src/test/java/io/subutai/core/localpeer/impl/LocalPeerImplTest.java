package io.subutai.core.localpeer.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerGateway;
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
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.settings.Common;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.localpeer.impl.dao.ResourceHostDataService;
import io.subutai.core.localpeer.impl.entity.ContainerHostEntity;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.repository.api.RepositoryManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.strategy.api.StrategyManager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
    private static final String SUBNET = "172.16.1.1/24";
    private static final String IP = "127.0.0.1";
    private static final Object REQUEST = new Object();
    private static final Object RESPONSE = new Object();
    private static final String RECIPIENT = "recipient";
    private static final int PID = 123;
    private static final int QUOTA = 123;
    private static final String ALIAS = "alias";
    private static final String CERT = "cert";
    private static final String N2N_IP = "10.11.0.1";
    private static final String RAMQUOTA = "123M";
    private static final String CPUQUOTA = "123";
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String ENV_ID = UUID.randomUUID().toString();
    private static final String NAME = "name";
    private static final String HOSTNAME = "hostname";
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String INTERFACE_NAME = "eth0";
    private static final String MAC = "mac";
    private static final int VLAN = 100;
    private static final int VNI = 10000;
    private static final int TUNNEL_ID = 123;

    @Mock
    NetworkManager networkManager;

    @Mock
    RepositoryManager repositoryManager;

    @Mock
    PeerManager peerManager;
    @Mock
    TemplateManager templateRegistry;
    @Mock
    Host managementHost;
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

    //    @Mock
    //    ContainerHostDataService containerHostDataService;
    //    @Mock
    //    ContainerGroupDataService containerGroupDataService;
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
    //    @Mock
    //    ContainerGroupEntity containerGroup;
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
    private ContainerGateway containerGateway;

    @Mock
    private ResourceValue cpuQuota;
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
        peerMap = new HashMap<>();
        peerMap.put( IP, N2N_IP );
        localPeer =
                spy( new LocalPeerImpl( daoManager, templateRegistry, quotaManager, strategyManager, commandExecutor,
                        hostRegistry, monitor, securityManager ) );

        //        localPeer.containerHostDataService = containerHostDataService;
        //        localPeer.containerGroupDataService = containerGroupDataService;
        localPeer.resourceHostDataService = resourceHostDataService;
        //        localPeer.managementHostDataService = managementHostDataService;
        localPeer.resourceHosts = Sets.newHashSet( ( ResourceHost ) resourceHost );
        localPeer.commandUtil = commandUtil;
        localPeer.exceptionUtil = exceptionUtil;
        localPeer.managementHost = managementHost;
        localPeer.requestListeners = Sets.newHashSet( requestListener );
        localPeer.setPeerInfo( peerInfo );

        //        when( cpuQuota.getValue( MeasureUnit.PERCENT ).intValue() ).thenReturn( Integer.parseInt( CPUQUOTA
        // ) );
        when( containerGateway.getContainerId() ).thenReturn( containerId );
        //        when(containerGateway.getGateway()).thenReturn(  );

        when( daoManager.getEntityManagerFactory() ).thenReturn( entityManagerFactory );
        when( managementHost.getId() ).thenReturn( MANAGEMENT_HOST_ID );
        when( managementHost.getHostInterfaces() ).thenReturn( hostInterfaces );
        when( resourceHost.getId() ).thenReturn( RESOURCE_HOST_ID );
        when( containerHost.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerHost.getContainerId() ).thenReturn( containerId );

        when( peerId.getId() ).thenReturn( PEER_ID );
        when( containerId.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerId.getPeerId() ).thenReturn( peerId );
        when( resourceHost.getContainerHostById( CONTAINER_HOST_ID ) ).thenReturn( containerHost );
        when( resourceHost.getHostname() ).thenReturn( RESOURCE_HOST_NAME );
        when( localPeer.getPeerInfo() ).thenReturn( peerInfo );
        when( securityManager.getKeyManager() ).thenReturn( keyManager );
        when( localPeer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getId() ).thenReturn( LOCAL_PEER_ID );
        when( peerInfo.getName() ).thenReturn( LOCAL_PEER_NAME );
        when( peerInfo.getOwnerId() ).thenReturn( OWNER_ID );
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
        //        when( containerGroup.getContainerIds() ).thenReturn( Sets.newHashSet( CONTAINER_HOST_ID ) );
        //        when( containerGroup.getOwnerId() ).thenReturn( OWNER_ID );
        //        when( containerGroup.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
        //        when( containerGroupDataService.getAll() ).thenReturn( Lists.newArrayList( containerGroup ) );
        doReturn( resourceHost ).when( localPeer ).getResourceHostByName( RESOURCE_HOST_NAME );
        doReturn( containerHost ).when( resourceHost ).getContainerHostByName( CONTAINER_HOST_NAME );
        when( containerHost.getParent() ).thenReturn( resourceHost );
        when( containerHostInfo.getState() ).thenReturn( ContainerHostState.RUNNING );
        when( containerHost.isConnected() ).thenReturn( true );
        when( resourceHost.getContainerHosts() ).thenReturn( Sets.<ContainerHost>newHashSet( containerHost ) );
        when( requestListener.getRecipient() ).thenReturn( RECIPIENT );
        doReturn( RESPONSE ).when( requestListener ).onRequest( any( Payload.class ) );

        peerMap = new HashMap<>();
        peerMap.put( IP, N2N_IP );
        when( environmentId.getId() ).thenReturn( ENV_ID );
        when( hostInfo.getId() ).thenReturn( HOST_ID );
        when( hostInfo.getHostname() ).thenReturn( HOSTNAME );
        when( hostInfo.getArch() ).thenReturn( ARCH );
        when( hostInterfaces.getAll() ).thenReturn( Sets.newHashSet( anHostInterface ) );
        when( hostInfo.getHostInterfaces() ).thenReturn( hostInterfaces );
        when( anHostInterface.getName() ).thenReturn( INTERFACE_NAME );
        when( anHostInterface.getIp() ).thenReturn( IP );
        when( anHostInterface.getMac() ).thenReturn( MAC );
        localPeer.singleThreadExecutorService = singleThreadExecutorService;
        localPeer.serviceLocator = serviceLocator;
        when( singleThreadExecutorService.submit( any( Callable.class ) ) ).thenReturn( future );
        when( serviceLocator.getService( RepositoryManager.class ) ).thenReturn( repositoryManager );
        when( serviceLocator.getService( NetworkManager.class ) ).thenReturn( networkManager );
//        when( localPeer.getManagementHost() ).thenReturn( managementHost );
    }


    @Test
    public void testInit() throws Exception
    {
        //        doReturn( managementHostDataService ).when( localPeer ).createManagementHostDataService();
        doReturn( resourceHostDataService ).when( localPeer ).createResourceHostDataService();
        //        doNothing().when( localPeer ).initPeerInfo( any( PeerDAO.class ) );

        localPeer.init();
    }

    //
    //    @Test
    //    public void testGetManagementHostDataService() throws Exception
    //    {
    //        assertNotNull( localPeer.createManagementHostDataService() );
    //    }


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
    public void testGetPeerInfo() throws Exception
    {
        assertEquals( peerInfo, localPeer.getPeerInfo() );
    }


    @Test
    @Ignore
    public void testGetContainerHostState() throws Exception
    {
        localPeer.getContainerState( containerHost.getContainerId() );

        verify( containerHost ).getState();
    }


    //    @Test
    //    public void testCreateContainer() throws Exception
    //    {
    //        localPeer.createContainer( resourceHost, template, CONTAINER_NAME );
    //
    //        verify( resourceHost ).createContainer( eq( TEMPLATE_NAME ), eq( CONTAINER_NAME ), anyInt() );
    //
    //        doThrow( new ResourceHostException( "" ) ).when( resourceHost )
    //                                                  .createContainer( eq( TEMPLATE_NAME ), eq( CONTAINER_NAME ),
    //                                                          anyInt() );
    //
    //        try
    //        {
    //            localPeer.createContainer( resourceHost, template, CONTAINER_NAME );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //
    //        when( templateRegistry.getTemplate( TEMPLATE_NAME ) ).thenReturn( null );
    //
    //
    //        try
    //        {
    //            localPeer.createContainer( resourceHost, template, CONTAINER_NAME );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //    }


    //    @Test( expected = ContainerGroupNotFoundException.class )
    //    public void testFindContainerGroupByContainerId() throws Exception
    //    {
    //        assertNotNull( localPeer.findContainerGroupByContainerId( CONTAINER_HOST_ID ) );
    //
    //        when( containerGroup.getContainerIds() ).thenReturn( Sets.<String>newHashSet() );
    //
    //        localPeer.findContainerGroupByContainerId( CONTAINER_HOST_ID );
    //    }

    //
    //    @Test
    //    public void testFindContainerGroupsByOwnerId() throws Exception
    //    {
    //        assertFalse( localPeer.findContainerGroupsByOwnerId( OWNER_ID ).isEmpty() );
    //
    //        when( containerGroup.getOwnerId() ).thenReturn( UUID.randomUUID().toString() );
    //
    //        assertTrue( localPeer.findContainerGroupsByOwnerId( OWNER_ID ).isEmpty() );
    //    }

    //
    //    @Test( expected = ContainerGroupNotFoundException.class )
    //    public void testFindContainerGroupByEnvironmentId() throws Exception
    //    {
    //        assertNotNull( localPeer.findContainerGroupByEnvironmentId( ENVIRONMENT_ID ) );
    //
    //        when( containerGroup.getEnvironmentId() ).thenReturn( UUID.randomUUID().toString() );
    //
    //        assertNull( localPeer.findContainerGroupByEnvironmentId( ENVIRONMENT_ID ) );
    //    }


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


    @Test
    @Ignore
    public void testGetContainerHostInfoById() throws Exception
    {
        assertNotNull( localPeer.getContainerHostInfoById( CONTAINER_HOST_ID ) );
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
        localPeer.destroyContainer( containerHost.getContainerId() );

        //        verify( containerGroupDataService ).remove( ENVIRONMENT_ID.toString() );

        //        when( containerGroup.getContainerIds() )
        //                .thenReturn( Sets.newHashSet( CONTAINER_HOST_ID, UUID.randomUUID().toString() ) );

        localPeer.destroyContainer( containerHost.getContainerId() );

        //        verify( containerGroupDataService ).update( containerGroup );

        //        ContainerGroupNotFoundException exception = mock( ContainerGroupNotFoundException.class );
        //        doThrow( exception ).when( localPeer ).findContainerGroupByContainerId( CONTAINER_HOST_ID );

        localPeer.destroyContainer( containerHost.getContainerId() );

        //        verify( exception ).printStackTrace( any( PrintStream.class ) );

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
        //localPeer.setDefaultGateway( containerGateway );

        verify( commandUtil ).execute( any( RequestBuilder.class ), eq( containerHost ) );

        throwCommandException();

        //localPeer.setDefaultGateway( containerGateway );
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

        //        verify( hostDisconnectedException ).printStackTrace( any( PrintStream.class ) );
    }


    @Test( expected = PeerException.class )
    @Ignore
    public void testGetQuotaInfo() throws Exception
    {
        localPeer.getQuota( containerHost, ResourceType.CPU );

        verify( quotaManager ).getQuota( containerId, ResourceType.CPU );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).getQuota( containerId, ResourceType.CPU );

        localPeer.getQuota( containerHost, ResourceType.CPU );
    }


    @Test( expected = PeerException.class )
    public void testSetQuota() throws Exception
    {
        ResourceValue quotaInfo = mock( ResourceValue.class );

        localPeer.setQuota( containerHost, ResourceType.RAM, quotaInfo );

        verify( quotaManager ).setQuota( containerId, ResourceType.RAM, quotaInfo );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).setQuota( containerId, ResourceType.RAM, quotaInfo );

        localPeer.setQuota( containerHost, ResourceType.RAM, quotaInfo );
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetManagementHost() throws Exception
    {
        assertEquals( managementHost, localPeer.getManagementHost() );

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

        verify( commandExecutor ).execute( CONTAINER_HOST_ID.toString(), requestBuilder, commandCallback );

        localPeer.execute( requestBuilder, containerHost );

        verify( commandExecutor ).execute( CONTAINER_HOST_ID.toString(), requestBuilder );

        when( containerHost.isConnected() ).thenReturn( false );

        try
        {
            localPeer.execute( requestBuilder, containerHost );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }

        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).bindHost( CONTAINER_HOST_ID );

        try
        {
            localPeer.execute( requestBuilder, containerHost );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }
    }


    @Test
    public void testExecuteAsync() throws Exception
    {
        localPeer.executeAsync( requestBuilder, containerHost, commandCallback );

        verify( commandExecutor ).executeAsync( CONTAINER_HOST_ID.toString(), requestBuilder, commandCallback );

        localPeer.executeAsync( requestBuilder, containerHost );

        verify( commandExecutor ).executeAsync( CONTAINER_HOST_ID.toString(), requestBuilder );

        when( containerHost.isConnected() ).thenReturn( false );

        try
        {
            localPeer.executeAsync( requestBuilder, containerHost );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }

        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).bindHost( CONTAINER_HOST_ID );

        try
        {
            localPeer.executeAsync( requestBuilder, containerHost );
            fail( "Expected CommandException" );
        }
        catch ( CommandException e )
        {
        }
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

        //        localPeer.updateResourceHostContainers( resourceHost, resourceHostInfo.getContainers() );

        resourceHost.updateHostInfo( resourceHostInfo );

        //        verify( containerHostDataService ).persist( any( ContainerHostEntity.class ) );
        //
        //        verify( containerHostDataService ).remove( CONTAINER_HOST_ID.toString() );

        when( resourceHostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo ) );

        //        doReturn( containerHost ).when( containerHostDataService ).find( anyString() );

        //        localPeer.updateResourceHostContainers( resourceHost, resourceHostInfo.getContainers() );
        resourceHost.updateHostInfo( resourceHostInfo );

        //        verify( containerHostDataService ).update( any( ContainerHostEntity.class ) );
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
        localPeer.getQuota( containerHost, ResourceType.RAM );

        verify( quotaManager ).getQuota( containerId, ResourceType.RAM );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).getQuota( containerId, ResourceType.RAM );

        localPeer.getQuota( containerHost, ResourceType.RAM );
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


    //    @Test
    //    public void testGetGateways() throws Exception
    //    {
    //        localPeer.getGateways();
    //
    //        verify( managementHost ).getGateways();
    //    }


//    @Test
//    public void testReserveVni() throws Exception
//    {
//        Vni vni = mock( Vni.class );
//
//        localPeer.reserveVni( vni );
//
//        verify( networkManager ).reserveVni( any( Vni.class ) );
//    }


    //    @Test
    //    public void testGetReservedVnis() throws Exception
    //    {
    //        localPeer.getReservedVnis();
    //
    //        verify( managementHost ).getReservedVnis();
    //    }
    //
    //
    //        @Test
    //        public void testSetupTunnels() throws Exception
    //        {
    //            localPeer.setupTunnels( peerMap, ENVIRONMENT_ID );
    //
    //            verify( managementHost ).setupTunnels( peerMap, ENVIRONMENT_ID );
    //        }


    //    @Mock
    //    Peer peer;
    //    @Mock
    //    ResourceHostInfo hostInfo;
    //    @Mock
    //    HostInterfaceModel anHostInterface;
    //    @Mock
    //    ExecutorService singleThreadExecutorService;
    //    @Mock
    //    ServiceLocator serviceLocator;


    //
    //    @Mock
    //    EnvironmentId environmentId;
    //
    //
    //    ManagementHostEntity managementHostEntity;
    //
    //    Map<String, String> peerMap = new HashMap<>();
    //    @Mock
    //    private HostInterfaces hostInterfaces;
    //
    //
    //    @Before
    //    public void setUp() throws Exception
    //    {


    //    }
    //
    //
    //    @Test
    //    public void testDispose() throws Exception
    //    {
    //        managementHostEntity.dispose();
    //
    //        verify( singleThreadExecutorService ).shutdown();
    //    }
    //
    //
    //    @Test
    //    public void testQueueSequentialTask() throws Exception
    //    {
    //        managementHostEntity.queueSequentialTask( callable );
    //
    //        verify( singleThreadExecutorService ).submit( callable );
    //    }
    //
    //
    //    @Test
    //    public void testGetNSetName() throws Exception
    //    {
    //        managementHostEntity.setName( NAME );
    //
    //        assertEquals( NAME, managementHostEntity.getName() );
    //    }
    //
    //
    //    @Test/*( expected = PeerException.class )*/ public void testAddAptSource() throws Exception
    //    {
    //        managementHostEntity.addRepository( IP );
    //
    //        verify( repositoryManager ).addRepository( IP );
    //
    //        doThrow( new RepositoryException( "" ) ).when( repositoryManager ).addRepository( anyString() );
    //
    //        //        managementHostEntity.addRepository( HOSTNAME, IP );
    //    }
    //
    //
    //    @Test/*( expected = PeerException.class )*/ public void testRemoveAptSource() throws Exception
    //    {
    //        managementHostEntity.removeRepository( HOSTNAME, IP );
    //
    //        verify( repositoryManager ).removeRepository( IP );
    //
    //        doThrow( new RepositoryException( "" ) ).when( repositoryManager ).removeRepository( anyString() );
    //        //
    //        //        managementHostEntity.removeRepository( HOSTNAME, IP );
    //    }
    //
    //
    //    @Test
    //    public void testCreateGateway() throws Exception
    //    {
    //        managementHostEntity.createGateway( IP, VLAN );
    //
    //        verify( future ).get();
    //
    //        doThrow( new InterruptedException() ).when( future ).get();
    //
    //        try
    //        {
    //            managementHostEntity.createGateway( IP, VLAN );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //
    //        doThrow( new ExecutionException( null ) ).when( future ).get();
    //
    //        try
    //        {
    //            managementHostEntity.createGateway( IP, VLAN );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //
    //        doThrow( new ExecutionException( new PeerException( "" ) ) ).when( future ).get();
    //
    //        try
    //        {
    //            managementHostEntity.createGateway( IP, VLAN );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //    }
    //
    //
    //    @Test
    //    @Ignore
    //    public void testReserveVni() throws Exception
    //    {
    //        Vni vni = mock( Vni.class );
    //        when( future.get() ).thenReturn( new Integer( VLAN ) );
    //
    //        managementHostEntity.reserveVni( vni );
    //
    //        verify( future ).get();
    //
    //        doThrow( new InterruptedException() ).when( future ).get();
    //
    //        try
    //        {
    //            managementHostEntity.reserveVni( vni );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //
    //        doThrow( new ExecutionException( null ) ).when( future ).get();
    //
    //        try
    //        {
    //            managementHostEntity.reserveVni( vni );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //
    //        doThrow( new ExecutionException( new PeerException( "" ) ) ).when( future ).get();
    //
    //        try
    //        {
    //            managementHostEntity.reserveVni( vni );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //    }
    //
    //
    //    @Test
    //    public void testSetupTunnels() throws Exception
    //    {
    //        when( future.get() ).thenReturn( new Integer( VLAN ) );
    //
    //        managementHostEntity.setupTunnels( peerMap, ENV_ID );
    //
    //        verify( future ).get();
    //
    //        doThrow( new InterruptedException() ).when( future ).get();
    //
    //        try
    //        {
    //            managementHostEntity.setupTunnels( peerMap, ENV_ID );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //
    //        doThrow( new ExecutionException( null ) ).when( future ).get();
    //
    //        try
    //        {
    //            managementHostEntity.setupTunnels( peerMap, ENV_ID );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //
    //        doThrow( new ExecutionException( new PeerException( "" ) ) ).when( future ).get();
    //
    //        try
    //        {
    //            managementHostEntity.setupTunnels( peerMap, ENV_ID );
    //            fail( "Expected PeerException" );
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //    }
    //
    //
    @Test( expected = PeerException.class )
    public void testRemoveGateway() throws Exception
    {
        localPeer.removeGateway( VLAN );

        verify( networkManager ).removeGateway( VLAN );

        doThrow( new NetworkManagerException( "" ) ).when( networkManager ).removeGateway( VLAN );

        localPeer.removeGateway( VLAN );
    }


    @Test( expected = PeerException.class )
    public void testCleanupEnvironmentNetworkSettings() throws Exception
    {
        localPeer.cleanupEnvironmentNetworkSettings( environmentId );

        verify( networkManager ).cleanupEnvironmentNetworkSettings( environmentId );

        doThrow( new NetworkManagerException( "" ) ).when( networkManager )
                                                    .cleanupEnvironmentNetworkSettings( environmentId );

        localPeer.cleanupEnvironmentNetworkSettings( environmentId );
    }


    @Test( expected = PeerException.class )
    public void testListTunnels() throws Exception
    {
        localPeer.listTunnels();

        verify( networkManager ).listTunnels();

        doThrow( new NetworkManagerException( "" ) ).when( networkManager ).listTunnels();

        localPeer.listTunnels();
    }


    @Test
    public void testRemoveTunnel() throws Exception
    {
        Tunnel tunnel = mock( Tunnel.class );
        when( networkManager.listTunnels() ).thenReturn( Sets.newHashSet( tunnel ) );
        when( tunnel.getTunnelIp() ).thenReturn( IP );

        localPeer.removeTunnel( IP );

        verify( networkManager ).removeTunnel( anyInt() );
    }


    //    @Test( expected = PeerException.class )
    //    public void testGetReservedVnis() throws Exception
    //    {
    //        managementHostEntity.getReservedVnis();
    //
    //        verify( networkManager ).getReservedVnis();
    //
    //        doThrow( new NetworkManagerException( "" ) ).when( networkManager ).getReservedVnis();
    //
    //        managementHostEntity.getReservedVnis();
    //    }


    @Test
    @Ignore
    public void testGetGateways() throws Exception
    {
        HostInterfaceModel hostInterfaceModelModel = mock( HostInterfaceModel.class );
        when( hostInterfaceModelModel.getName() ).thenReturn( "br-100" );

        Set<Gateway> gateways = localPeer.getGateways();

        assertFalse( gateways.isEmpty() );
    }


    @Test
    public void testFindVniByEnvironmentById() throws Exception
    {

        assertNull( localPeer.findVniByEnvironmentId( ENV_ID ) );

        Vni vni = mock( Vni.class );
        when( vni.getEnvironmentId() ).thenReturn( ENV_ID );
        when( networkManager.getReservedVnis() ).thenReturn( Sets.newHashSet( vni ) );

        Vni vni2 = localPeer.findVniByEnvironmentId( ENV_ID );

        assertEquals( vni, vni2 );
    }
    //
    //
    //        @Test( expected = PeerException.class )
    //        public void testSetupVniVlanMapping() throws Exception
    //        {
    //            VniVlanMapping vniVlanMapping = mock( VniVlanMapping.class );
    //            when( networkManager.getVniVlanMappings() ).thenReturn( Sets.newHashSet( vniVlanMapping ) );
    //            when( vniVlanMapping.getTunnelId() ).thenReturn( TUNNEL_ID );
    //            when( vniVlanMapping.getEnvironmentId() ).thenReturn( ENV_ID );
    //
    //            managementHostEntity.setupVniVlanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );
    //
    //            verify( networkManager, never() ).setupVniVLanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );
    //
    //            when( vniVlanMapping.getTunnelId() ).thenReturn( TUNNEL_ID + 1 );
    //
    //            managementHostEntity.setupVniVlanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );
    //
    //            verify( networkManager ).setupVniVLanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );
    //
    //            doThrow( new NetworkManagerException( "" ) ).when( networkManager )
    //                                                        .setupVniVLanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );
    //
    //            managementHostEntity.setupVniVlanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );
    //        }
    //
    //
    //    @Test
    //    public void testFindTunnel() throws Exception
    //    {
    //        Tunnel tunnel = mock( Tunnel.class );
    //        when( tunnel.getTunnelIp() ).thenReturn( IP );
    //        when( tunnel.getTunnelId() ).thenReturn( TUNNEL_ID );
    //
    //        int tunnelId = managementHostEntity.findTunnel( IP, Sets.newHashSet( tunnel ) );
    //
    //        assertEquals( TUNNEL_ID, tunnelId );
    //
    //        when( tunnel.getTunnelIp() ).thenReturn( "" );
    //
    //        tunnelId = managementHostEntity.findTunnel( IP, Sets.newHashSet( tunnel ) );
    //
    //        assertThat( TUNNEL_ID, not( tunnelId ) );
    //    }
    //
    //
    //    @Test
    //    public void testCalculateNextTunnelId() throws Exception
    //    {
    //        Tunnel tunnel = mock( Tunnel.class );
    //        when( tunnel.getTunnelId() ).thenReturn( TUNNEL_ID );
    //
    //        int newTunnelId = managementHostEntity.calculateNextTunnelId( Sets.newHashSet( tunnel ) );
    //
    //        assertEquals( ( TUNNEL_ID + 1 ), newTunnelId );
    //    }
    //


    //    @Test
    //    public void testFindAvailableVlanId() throws Exception
    //    {
    //        Vni vni = mock( Vni.class );
    //        when( vni.getVlan() ).thenReturn( VLAN );
    //        when( networkManager.getReservedVnis() ).thenReturn( Sets.newHashSet( vni ) );
    //
    //        int newVlanId = localPeer.findAvailableVlanId();
    //
    //        assertEquals( ( VLAN + 1 ), newVlanId );
    //    }
}
