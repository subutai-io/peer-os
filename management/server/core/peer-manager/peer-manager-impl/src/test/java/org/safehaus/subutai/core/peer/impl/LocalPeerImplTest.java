package org.safehaus.subutai.core.peer.impl;


import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.environment.CreateContainerGroupRequest;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.ContainersDestructionResult;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.QuotaException;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.quota.RamQuota;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.ExceptionUtil;
import io.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostDisconnectedException;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.peer.api.ContainerGroupNotFoundException;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.core.peer.impl.container.DestroyContainerWrapperTask;
import org.safehaus.subutai.core.peer.impl.dao.ContainerGroupDataService;
import org.safehaus.subutai.core.peer.impl.dao.ContainerHostDataService;
import org.safehaus.subutai.core.peer.impl.dao.ManagementHostDataService;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.peer.impl.dao.ResourceHostDataService;
import org.safehaus.subutai.core.peer.impl.entity.ContainerGroupEntity;
import org.safehaus.subutai.core.peer.impl.entity.ContainerHostEntity;
import org.safehaus.subutai.core.peer.impl.entity.ManagementHostEntity;
import org.safehaus.subutai.core.peer.impl.entity.ResourceHostEntity;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.ssl.manager.api.SubutaiSslContextFactory;
import org.safehaus.subutai.core.strategy.api.StrategyException;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.strategy.api.StrategyNotFoundException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
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
    private static final UUID ENVIRONMENT_ID = UUID.randomUUID();
    private static final UUID LOCAL_PEER_ID = UUID.randomUUID();
    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final String LOCAL_PEER_NAME = "local peer";
    private static final UUID MANAGEMENT_HOST_ID = UUID.randomUUID();
    private static final UUID RESOURCE_HOST_ID = UUID.randomUUID();
    private static final UUID CONTAINER_HOST_ID = UUID.randomUUID();
    private static final String RESOURCE_HOST_NAME = "foo";
    private static final String CONTAINER_NAME = "foo";
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


    @Mock
    PeerManager peerManager;
    @Mock
    TemplateRegistry templateRegistry;
    @Mock
    ManagementHostEntity managementHost;
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
    ManagementHostDataService managementHostDataService;
    @Mock
    HostRegistry hostRegistry;
    @Mock
    RequestListener requestListener;

    @Mock
    ContainerHostDataService containerHostDataService;
    @Mock
    ContainerGroupDataService containerGroupDataService;
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
    ContainerHostEntity containerHost;
    @Mock
    ContainerHostInfo containerHostInfo;
    @Mock
    Template template;
    @Mock
    HostInfoModel hostInfoModel;
    @Mock
    ContainerGroupEntity containerGroup;
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
    SubutaiSslContextFactory subutaiSslContextFactory;
    @Mock
    DaoManager daoManager;

    LocalPeerImpl localPeer;


    @Before
    public void setUp() throws Exception
    {
        localPeer =
                spy( new LocalPeerImpl( daoManager, templateRegistry, quotaManager, strategyManager, commandExecutor,
                        hostRegistry, monitor, subutaiSslContextFactory ) );

        localPeer.containerHostDataService = containerHostDataService;
        localPeer.containerGroupDataService = containerGroupDataService;
        localPeer.resourceHostDataService = resourceHostDataService;
        localPeer.managementHostDataService = managementHostDataService;
        localPeer.resourceHosts = Sets.newHashSet( ( ResourceHost ) resourceHost );
        localPeer.commandUtil = commandUtil;
        localPeer.exceptionUtil = exceptionUtil;
        localPeer.managementHost = managementHost;
        localPeer.requestListeners = Sets.newHashSet( requestListener );
        when( managementHost.getId() ).thenReturn( MANAGEMENT_HOST_ID );
        when( managementHost.getHostId() ).thenReturn( MANAGEMENT_HOST_ID.toString() );
        when( resourceHost.getId() ).thenReturn( RESOURCE_HOST_ID );
        when( resourceHost.getHostId() ).thenReturn( RESOURCE_HOST_ID.toString() );
        when( containerHost.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerHost.getHostId() ).thenReturn( CONTAINER_HOST_ID.toString() );
        when( resourceHost.getContainerHostById( CONTAINER_HOST_ID ) ).thenReturn( containerHost );
        when( resourceHost.getHostname() ).thenReturn( RESOURCE_HOST_NAME );
        when( localPeer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getId() ).thenReturn( LOCAL_PEER_ID );
        when( peerInfo.getName() ).thenReturn( LOCAL_PEER_NAME );
        when( peerInfo.getOwnerId() ).thenReturn( OWNER_ID );
        when( resourceHostDataService.getAll() ).thenReturn( Sets.newHashSet( resourceHost ) );
        when( managementHostDataService.getAll() ).thenReturn( Sets.newHashSet( managementHost ) );
        when( templateRegistry.getTemplate( TEMPLATE_NAME ) ).thenReturn( template );
        when( template.getTemplateName() ).thenReturn( TEMPLATE_NAME );
        when( resourceHost.isConnected() ).thenReturn( true );
        when( hostRegistry.getContainerHostInfoById( CONTAINER_HOST_ID ) ).thenReturn( containerHostInfo );
        when( hostRegistry.getHostInfoById( CONTAINER_HOST_ID ) ).thenReturn( containerHostInfo );
        when( containerHostInfo.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerHost.getHostname() ).thenReturn( CONTAINER_NAME );
        when( containerHost.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID.toString() );
        when( containerGroup.getContainerIds() ).thenReturn( Sets.newHashSet( CONTAINER_HOST_ID ) );
        when( containerGroup.getOwnerId() ).thenReturn( OWNER_ID );
        when( containerGroup.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
        when( containerGroupDataService.getAll() ).thenReturn( Lists.newArrayList( containerGroup ) );
        doReturn( resourceHost ).when( localPeer ).getResourceHostByName( RESOURCE_HOST_NAME );
        doReturn( containerHost ).when( resourceHost ).getContainerHostByName( CONTAINER_NAME );
        when( containerHost.getParent() ).thenReturn( resourceHost );
        when( containerHostInfo.getStatus() ).thenReturn( ContainerHostState.RUNNING );
        when( containerHost.isConnected() ).thenReturn( true );
        when( resourceHost.getContainerHosts() ).thenReturn( Sets.<ContainerHost>newHashSet( containerHost ) );
        when( requestListener.getRecipient() ).thenReturn( RECIPIENT );
        doReturn( RESPONSE ).when( requestListener ).onRequest( any( Payload.class ) );
    }


    @Test
    public void testInit() throws Exception
    {
        doReturn( managementHostDataService ).when( localPeer ).getManagementHostDataService();
        doReturn( resourceHostDataService ).when( localPeer ).getResourceHostDataService();
        doNothing().when( localPeer ).initPeerInfo( any( PeerDAO.class) );

        localPeer.init();
    }


    @Test
    public void testGetManagementHostDataService() throws Exception
    {
        assertNotNull( localPeer.getManagementHostDataService() );
    }


    @Test
    public void testGetResourceHostDataService() throws Exception
    {
        assertNotNull( localPeer.getResourceHostDataService() );
    }


    @Test
    public void testDispose() throws Exception
    {
        localPeer.dispose();

        verify( managementHost ).dispose();

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
    public void testGetContainerHostState() throws Exception
    {
        localPeer.getContainerHostState( containerHost );

        verify( containerHost ).getState();
    }


    @Test
    public void testCreateContainer() throws Exception
    {
        localPeer.createContainer( resourceHost, template, CONTAINER_NAME );

        verify( resourceHost ).createContainer( eq( TEMPLATE_NAME ), eq( CONTAINER_NAME ), anyInt() );

        doThrow( new ResourceHostException( "" ) ).when( resourceHost )
                                                  .createContainer( eq( TEMPLATE_NAME ), eq( CONTAINER_NAME ),
                                                          anyInt() );

        try
        {
            localPeer.createContainer( resourceHost, template, CONTAINER_NAME );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        when( templateRegistry.getTemplate( TEMPLATE_NAME ) ).thenReturn( null );


        try
        {
            localPeer.createContainer( resourceHost, template, CONTAINER_NAME );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test
    public void testCreateContainerGroup() throws Exception
    {
        Map<ResourceHost, Set<String>> distribution = Maps.newHashMap();
        distribution.put( resourceHost, Sets.newHashSet( CONTAINER_NAME ) );
        doReturn( distribution ).when( localPeer )
                                .distributeContainersToResourceHosts( any( CreateContainerGroupRequest.class ) );
        doReturn( Common.MIN_VLAN_ID ).when( localPeer ).setupTunnels( anySet(), any( UUID.class ) );
        doReturn( Sets.newHashSet( hostInfoModel ) ).when( localPeer )
                                                    .processRequestCompletion( anyList(), any( ExecutorService.class ),
                                                            any( CreateContainerGroupRequest.class ) );

        localPeer.createContainerGroup(
                new CreateContainerGroupRequest( Sets.newHashSet( IP ), ENVIRONMENT_ID, UUID.randomUUID(), OWNER_ID,
                        SUBNET, Lists.newArrayList( template ), 1, "ROUND_ROBIN", Lists.<Criteria>newArrayList(), 0 ) );

        try
        {
            localPeer.createContainerGroup(
                    new CreateContainerGroupRequest( Sets.newHashSet( IP ), ENVIRONMENT_ID, UUID.randomUUID(), OWNER_ID,
                            "", Lists.newArrayList( template ), 1, "ROUND_ROBIN", Lists.<Criteria>newArrayList(), 0 ) );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        doThrow( new StrategyNotFoundException( "" ) ).when( strategyManager ).findStrategyById( anyString() );

        try
        {
            localPeer.createContainerGroup(
                    new CreateContainerGroupRequest( Sets.newHashSet( IP ), ENVIRONMENT_ID, UUID.randomUUID(), OWNER_ID,
                            SUBNET, Lists.newArrayList( template ), 1, "ROUND_ROBIN", Lists.<Criteria>newArrayList(),
                            0 ) );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test( expected = PeerException.class )
    public void testRegisterRemoteTemplates() throws Exception
    {
        CreateContainerGroupRequest request = mock( CreateContainerGroupRequest.class );
        when( request.getTemplates() ).thenReturn( Lists.newArrayList( template ) );
        when( template.isRemote() ).thenReturn( true );
        when( templateRegistry.getTemplate( TEMPLATE_NAME ) ).thenReturn( null );


        localPeer.registerRemoteTemplates( request );

        verify( templateRegistry ).registerTemplate( template );

        doThrow( new RegistryException( "" ) ).when( templateRegistry ).registerTemplate( template );

        localPeer.registerRemoteTemplates( request );
    }


    @Test
    public void testDistributeContainersToResourceHosts() throws Exception
    {
        CreateContainerGroupRequest request = mock( CreateContainerGroupRequest.class );
        when( request.getTemplates() ).thenReturn( Lists.newArrayList( template ) );
        ResourceHostMetric resourceHostMetric = mock( ResourceHostMetric.class );
        when( resourceHostMetric.getHost() ).thenReturn( RESOURCE_HOST_NAME );

        Map<ResourceHostMetric, Integer> placementMap = Maps.newHashMap();
        placementMap.put( resourceHostMetric, 1 );
        doReturn( placementMap ).when( strategyManager )
                                .getPlacementDistribution( anyList(), anyInt(), anyString(), anyList() );

        Map<ResourceHost, Set<String>> distribution = localPeer.distributeContainersToResourceHosts( request );

        assertTrue( distribution.keySet().contains( resourceHost ) );

        doThrow( new StrategyException( "" ) ).when( strategyManager )
                                              .getPlacementDistribution( anyList(), anyInt(), anyString(), anyList() );

        try
        {
            localPeer.distributeContainersToResourceHosts( request );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        doThrow( new ResourceHostException( "" ) ).when( resourceHost ).getHostMetric();

        try
        {
            localPeer.distributeContainersToResourceHosts( request );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test
    public void testProcessRequestCompletion() throws Exception
    {
        Future<ContainerHost> taskFuture = mock( Future.class );
        ExecutorService executorService = mock( ExecutorService.class );
        CreateContainerGroupRequest request = mock( CreateContainerGroupRequest.class );
        when( taskFuture.get() ).thenReturn( containerHost );
        when( request.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
        when( request.getOwnerId() ).thenReturn( OWNER_ID );
        when( request.getInitiatorPeerId() ).thenReturn( UUID.randomUUID() );
        doReturn( containerGroup ).when( localPeer ).findContainerGroupByEnvironmentId( ENVIRONMENT_ID );

        localPeer.processRequestCompletion( Lists.newArrayList( taskFuture ), executorService, request );

        when( request.getEnvironmentId() ).thenReturn( UUID.randomUUID() );

        localPeer.processRequestCompletion( Lists.newArrayList( taskFuture ), executorService, request );

        ExecutionException exception = mock( ExecutionException.class );
        doThrow( exception ).when( taskFuture ).get();

        localPeer.processRequestCompletion( Lists.newArrayList( taskFuture ), executorService, request );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test( expected = ContainerGroupNotFoundException.class )
    public void testFindContainerGroupByContainerId() throws Exception
    {
        assertNotNull( localPeer.findContainerGroupByContainerId( CONTAINER_HOST_ID ) );

        when( containerGroup.getContainerIds() ).thenReturn( Sets.<UUID>newHashSet() );

        localPeer.findContainerGroupByContainerId( CONTAINER_HOST_ID );
    }


    @Test
    public void testFindContainerGroupsByOwnerId() throws Exception
    {
        assertFalse( localPeer.findContainerGroupsByOwnerId( OWNER_ID ).isEmpty() );

        when( containerGroup.getOwnerId() ).thenReturn( UUID.randomUUID() );

        assertTrue( localPeer.findContainerGroupsByOwnerId( OWNER_ID ).isEmpty() );
    }


    @Test( expected = ContainerGroupNotFoundException.class )
    public void testFindContainerGroupByEnvironmentId() throws Exception
    {
        assertNotNull( localPeer.findContainerGroupByEnvironmentId( ENVIRONMENT_ID ) );

        when( containerGroup.getEnvironmentId() ).thenReturn( UUID.randomUUID() );

        assertNull( localPeer.findContainerGroupByEnvironmentId( ENVIRONMENT_ID ) );
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetContainerHostByName() throws Exception
    {
        assertEquals( containerHost, localPeer.getContainerHostByName( CONTAINER_NAME ) );

        doThrow( new HostNotFoundException( "" ) ).when( resourceHost ).getContainerHostByName( CONTAINER_NAME );

        localPeer.getContainerHostByName( CONTAINER_NAME );
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetContainerHostById() throws Exception
    {
        assertEquals( containerHost, localPeer.getContainerHostById( CONTAINER_HOST_ID ) );

        doThrow( new HostNotFoundException( "" ) ).when( resourceHost ).getContainerHostById( CONTAINER_HOST_ID );

        localPeer.getContainerHostById( CONTAINER_HOST_ID );
    }


    @Test
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

        localPeer.getResourceHostById( UUID.randomUUID() );
    }


    @Test
    public void testGetResourceHostByContainerName() throws Exception
    {
        assertEquals( resourceHost, localPeer.getResourceHostByContainerName( CONTAINER_NAME ) );
    }


    @Test
    public void testGetResourceHostByContainerId() throws Exception
    {
        assertEquals( resourceHost, localPeer.getResourceHostByContainerId( CONTAINER_HOST_ID ) );
    }


    @Test
    public void testBindHost() throws Exception
    {

        assertEquals( managementHost, localPeer.bindHost( MANAGEMENT_HOST_ID ) );

        assertEquals( resourceHost, localPeer.bindHost( RESOURCE_HOST_ID ) );

        assertEquals( containerHost, localPeer.bindHost( CONTAINER_HOST_ID ) );
    }


    @Test( expected = PeerException.class )
    public void testStartContainer() throws Exception
    {
        localPeer.startContainer( containerHost );

        verify( resourceHost ).startContainerHost( containerHost );

        RuntimeException cause = mock( RuntimeException.class );

        doThrow( cause ).when( resourceHost ).startContainerHost( containerHost );

        localPeer.startContainer( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testStopContainer() throws Exception
    {
        localPeer.stopContainer( containerHost );

        verify( resourceHost ).stopContainerHost( containerHost );

        RuntimeException cause = mock( RuntimeException.class );

        doThrow( cause ).when( resourceHost ).stopContainerHost( containerHost );

        localPeer.stopContainer( containerHost );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {
        localPeer.destroyContainer( containerHost );

        verify( containerGroupDataService ).remove( ENVIRONMENT_ID.toString() );

        when( containerGroup.getContainerIds() ).thenReturn( Sets.newHashSet( CONTAINER_HOST_ID, UUID.randomUUID() ) );

        localPeer.destroyContainer( containerHost );

        verify( containerGroupDataService ).update( containerGroup );

        ContainerGroupNotFoundException exception = mock( ContainerGroupNotFoundException.class );
        doThrow( exception ).when( localPeer ).findContainerGroupByContainerId( CONTAINER_HOST_ID );

        localPeer.destroyContainer( containerHost );

        verify( exception ).printStackTrace( any( PrintStream.class ) );

        doThrow( new ResourceHostException( "" ) ).when( resourceHost ).destroyContainerHost( containerHost );

        try
        {
            localPeer.destroyContainer( containerHost );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test
    public void testCleanupEnvironmentNetworkSettings() throws Exception
    {
        localPeer.cleanupEnvironmentNetworkSettings( containerGroup );

        verify( managementHost ).cleanupEnvironmentNetworkSettings( ENVIRONMENT_ID );

        PeerException peerException = mock( PeerException.class );
        doThrow( peerException ).when( managementHost ).cleanupEnvironmentNetworkSettings( ENVIRONMENT_ID );

        localPeer.cleanupEnvironmentNetworkSettings( containerGroup );

        verify( exceptionUtil ).getRootCause( peerException );
    }


    private void throwCommandException() throws CommandException
    {
        doThrow( commandException ).when( commandUtil ).execute( any( RequestBuilder.class ), any( Host.class ) );
    }


    @Test( expected = PeerException.class )
    public void testSetDefaultGateway() throws Exception
    {
        localPeer.setDefaultGateway( containerHost, IP );

        verify( commandUtil ).execute( any( RequestBuilder.class ), eq( containerHost ) );

        throwCommandException();

        localPeer.setDefaultGateway( containerHost, IP );
    }


    @Test
    public void testIsConnected() throws Exception
    {
        assertTrue( localPeer.isConnected( containerHost ) );

        when( hostRegistry.getHostInfoById( CONTAINER_HOST_ID ) ).thenReturn( hostInfo );

        assertFalse( localPeer.isConnected( containerHost ) );

        HostDisconnectedException hostDisconnectedException = mock( HostDisconnectedException.class );

        doThrow( hostDisconnectedException ).when( hostRegistry ).getHostInfoById( CONTAINER_HOST_ID );

        assertFalse( localPeer.isConnected( containerHost ) );

        verify( hostDisconnectedException ).printStackTrace( any( PrintStream.class ) );
    }


    @Test( expected = PeerException.class )
    public void testGetQuotaInfo() throws Exception
    {
        localPeer.getQuotaInfo( containerHost, QuotaType.QUOTA_TYPE_CPU );

        verify( quotaManager ).getQuotaInfo( CONTAINER_HOST_ID, QuotaType.QUOTA_TYPE_CPU );

        doThrow( new QuotaException( "" ) ).when( quotaManager )
                                           .getQuotaInfo( CONTAINER_HOST_ID, QuotaType.QUOTA_TYPE_CPU );

        localPeer.getQuotaInfo( containerHost, QuotaType.QUOTA_TYPE_CPU );
    }


    @Test( expected = PeerException.class )
    public void testSetQuota() throws Exception
    {
        QuotaInfo quotaInfo = mock( QuotaInfo.class );

        localPeer.setQuota( containerHost, quotaInfo );

        verify( quotaManager ).setQuota( CONTAINER_NAME, quotaInfo );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).setQuota( CONTAINER_NAME, quotaInfo );

        localPeer.setQuota( containerHost, quotaInfo );
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

        verify( commandExecutor ).execute( CONTAINER_HOST_ID, requestBuilder, commandCallback );

        localPeer.execute( requestBuilder, containerHost );

        verify( commandExecutor ).execute( CONTAINER_HOST_ID, requestBuilder );

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

        verify( commandExecutor ).executeAsync( CONTAINER_HOST_ID, requestBuilder, commandCallback );

        localPeer.executeAsync( requestBuilder, containerHost );

        verify( commandExecutor ).executeAsync( CONTAINER_HOST_ID, requestBuilder );

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
    public void testCleanDb() throws Exception
    {
        localPeer.cleanDb();

        verify( managementHostDataService ).remove( MANAGEMENT_HOST_ID.toString() );

        verify( resourceHostDataService ).remove( RESOURCE_HOST_ID.toString() );

        verify( containerHostDataService ).remove( CONTAINER_HOST_ID.toString() );
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

        localPeer.onHeartbeat( resourceHostInfo );

        verify( managementHost ).updateHostInfo( resourceHostInfo );

        localPeer.managementHost = null;

        localPeer.onHeartbeat( resourceHostInfo );

        verify( managementHostDataService ).persist( any( ManagementHostEntity.class ) );

        when( resourceHostInfo.getHostname() ).thenReturn( RESOURCE_HOST_NAME );
        when( resourceHostInfo.getId() ).thenReturn( RESOURCE_HOST_ID );

        localPeer.onHeartbeat( resourceHostInfo );

        verify( resourceHost ).updateHostInfo( resourceHostInfo );

        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).getResourceHostByName( RESOURCE_HOST_NAME );

        localPeer.onHeartbeat( resourceHostInfo );

        verify( resourceHostDataService ).persist( any( ResourceHostEntity.class ) );
    }


    @Test
    public void testSaveResourceHostContainers() throws Exception
    {

        ContainerHostInfo containerHostInfo1 = mock( ContainerHostInfo.class );
        when( containerHostInfo1.getId() ).thenReturn( UUID.randomUUID() );

        when( resourceHostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo1 ) );

        localPeer.saveResourceHostContainers( resourceHost, resourceHostInfo.getContainers() );

        verify( containerHostDataService ).persist( any( ContainerHostEntity.class ) );

        verify( containerHostDataService ).remove( CONTAINER_HOST_ID.toString() );

        when( resourceHostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo ) );

        localPeer.saveResourceHostContainers( resourceHost, resourceHostInfo.getContainers() );

        verify( containerHostDataService ).update( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testGetProcessResourceUsage() throws Exception
    {
        localPeer.getProcessResourceUsage( containerHost, PID );

        verify( monitor ).getProcessResourceUsage( containerHost, PID );

        doThrow( new MonitorException( "" ) ).when( monitor ).getProcessResourceUsage( containerHost, PID );

        localPeer.getProcessResourceUsage( containerHost, PID );
    }


    @Test( expected = PeerException.class )
    public void testGetRamQuota() throws Exception
    {
        localPeer.getRamQuota( containerHost );

        verify( quotaManager ).getRamQuota( CONTAINER_HOST_ID );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).getRamQuota( CONTAINER_HOST_ID );

        localPeer.getRamQuota( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testGetRamQuotaInfo() throws Exception
    {
        localPeer.getRamQuotaInfo( containerHost );

        verify( quotaManager ).getRamQuotaInfo( CONTAINER_HOST_ID );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).getRamQuotaInfo( CONTAINER_HOST_ID );

        localPeer.getRamQuotaInfo( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testSetRamQuota() throws Exception
    {
        localPeer.setRamQuota( containerHost, QUOTA );

        verify( quotaManager ).setRamQuota( CONTAINER_HOST_ID, QUOTA );

        doThrow( new QuotaException( "" ) ).when( quotaManager ).setRamQuota( CONTAINER_HOST_ID, QUOTA );

        localPeer.setRamQuota( containerHost, QUOTA );
    }


    @Test( expected = PeerException.class )
    public void testGetCpuQuota() throws Exception
    {
        localPeer.getCpuQuota( containerHost );

        verify( quotaManager ).getCpuQuota( CONTAINER_HOST_ID );

        doThrow( new QuotaException() ).when( quotaManager ).getCpuQuota( CONTAINER_HOST_ID );

        localPeer.getCpuQuota( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testGetCpuQuotaInfo() throws Exception
    {
        localPeer.getCpuQuotaInfo( containerHost );

        verify( quotaManager ).getCpuQuotaInfo( CONTAINER_HOST_ID );

        doThrow( new QuotaException() ).when( quotaManager ).getCpuQuotaInfo( CONTAINER_HOST_ID );

        localPeer.getCpuQuotaInfo( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testSetCpuQuota() throws Exception
    {
        localPeer.setCpuQuota( containerHost, QUOTA );

        verify( quotaManager ).setCpuQuota( CONTAINER_HOST_ID, QUOTA );

        doThrow( new QuotaException() ).when( quotaManager ).setCpuQuota( CONTAINER_HOST_ID, QUOTA );

        localPeer.setCpuQuota( containerHost, QUOTA );
    }


    @Test( expected = PeerException.class )
    public void testGetCpuSet() throws Exception
    {
        localPeer.getCpuSet( containerHost );

        verify( quotaManager ).getCpuSet( CONTAINER_HOST_ID );

        doThrow( new QuotaException() ).when( quotaManager ).getCpuSet( CONTAINER_HOST_ID );

        localPeer.getCpuSet( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testSetCpuSet() throws Exception
    {
        localPeer.setCpuSet( containerHost, Sets.newHashSet( QUOTA ) );

        verify( quotaManager ).setCpuSet( eq( CONTAINER_HOST_ID ), anySet() );

        doThrow( new QuotaException() ).when( quotaManager ).setCpuSet( eq( CONTAINER_HOST_ID ), anySet() );

        localPeer.setCpuSet( containerHost, Sets.newHashSet( QUOTA ) );
    }


    @Test( expected = PeerException.class )
    public void testGetDiskQuota() throws Exception
    {
        localPeer.getDiskQuota( containerHost, DiskPartition.VAR );

        verify( quotaManager ).getDiskQuota( CONTAINER_HOST_ID, DiskPartition.VAR );

        doThrow( new QuotaException() ).when( quotaManager ).getDiskQuota( CONTAINER_HOST_ID, DiskPartition.VAR );

        localPeer.getDiskQuota( containerHost, DiskPartition.VAR );
    }


    @Test( expected = PeerException.class )
    public void testSetDiskQuota() throws Exception
    {
        DiskQuota diskQuota = mock( DiskQuota.class );

        localPeer.setDiskQuota( containerHost, diskQuota );

        verify( quotaManager ).setDiskQuota( CONTAINER_HOST_ID, diskQuota );

        doThrow( new QuotaException() ).when( quotaManager ).setDiskQuota( CONTAINER_HOST_ID, diskQuota );

        localPeer.setDiskQuota( containerHost, diskQuota );
    }


    @Test( expected = PeerException.class )
    public void testSetRamQuota2() throws Exception
    {
        RamQuota ramQuota = mock( RamQuota.class );

        localPeer.setRamQuota( containerHost, ramQuota );

        verify( quotaManager ).setRamQuota( CONTAINER_HOST_ID, ramQuota );

        doThrow( new QuotaException() ).when( quotaManager ).setRamQuota( CONTAINER_HOST_ID, ramQuota );

        localPeer.setRamQuota( containerHost, ramQuota );
    }


    @Test( expected = PeerException.class )
    public void testGetAvailableRamQuota() throws Exception
    {
        localPeer.getAvailableRamQuota( containerHost );

        verify( quotaManager ).getAvailableRamQuota( CONTAINER_HOST_ID );

        doThrow( new QuotaException() ).when( quotaManager ).getAvailableRamQuota( CONTAINER_HOST_ID );

        localPeer.getAvailableRamQuota( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testGetAvailableCpuQuota() throws Exception
    {
        localPeer.getAvailableCpuQuota( containerHost );

        verify( quotaManager ).getAvailableCpuQuota( CONTAINER_HOST_ID );

        doThrow( new QuotaException() ).when( quotaManager ).getAvailableCpuQuota( CONTAINER_HOST_ID );

        localPeer.getAvailableCpuQuota( containerHost );
    }


    @Test( expected = PeerException.class )
    public void testGetAvailableDiskQuota() throws Exception
    {
        localPeer.getAvailableDiskQuota( containerHost, DiskPartition.VAR );

        verify( quotaManager ).getAvailableDiskQuota( CONTAINER_HOST_ID, DiskPartition.VAR );

        doThrow( new QuotaException() ).when( quotaManager )
                                       .getAvailableDiskQuota( CONTAINER_HOST_ID, DiskPartition.VAR );

        localPeer.getAvailableDiskQuota( containerHost, DiskPartition.VAR );
    }


    @Test
    public void testDestroyEnvironmentContainers() throws Exception
    {
        ExecutorService executorService = mock( ExecutorService.class );
        doReturn( executorService ).when( localPeer ).getFixedExecutor( anyInt() );
        Future<UUID> future = mock( Future.class );
        doReturn( future ).when( executorService ).submit( any( DestroyContainerWrapperTask.class ) );
        doReturn( CONTAINER_HOST_ID ).when( future ).get();

        ContainersDestructionResult result = localPeer.destroyEnvironmentContainers( ENVIRONMENT_ID );

        assertTrue( result.getDestroyedContainersIds().contains( CONTAINER_HOST_ID ) );

        verify( managementHost ).cleanupEnvironmentNetworkSettings( ENVIRONMENT_ID );
    }


    @Test
    public void testGetGateways() throws Exception
    {
        localPeer.getGateways();

        verify( managementHost ).getGateways();
    }


    @Test
    public void testReserveVni() throws Exception
    {
        Vni vni = mock( Vni.class );

        localPeer.reserveVni( vni );

        verify( managementHost ).reserveVni( vni );
    }


    @Test
    public void testGetReservedVnis() throws Exception
    {
        localPeer.getReservedVnis();

        verify( managementHost ).getReservedVnis();
    }


    @Test
    public void testSetupTunnels() throws Exception
    {
        localPeer.setupTunnels( Sets.newHashSet( IP ), ENVIRONMENT_ID );

        verify( managementHost ).setupTunnels( Sets.newHashSet( IP ), ENVIRONMENT_ID );
    }


    @Test
    public void testImportCertificate() throws Exception
    {
        localPeer.importCertificate( CERT, ALIAS );

        verify( subutaiSslContextFactory ).reloadTrustStore();
    }


    @Test
    public void testExportEnvironmentCertificate() throws Exception
    {
        localPeer.exportEnvironmentCertificate( ENVIRONMENT_ID );

        verify( subutaiSslContextFactory ).reloadKeyStore();
    }


    @Test
    public void testRemoveEnvironmentCertificates() throws Exception
    {
        localPeer.removeEnvironmentCertificates( ENVIRONMENT_ID );
    }
}
