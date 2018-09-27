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
import io.subutai.common.protocol.Template;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.settings.Common;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.localpeer.impl.dao.ResourceHostDataService;
import io.subutai.core.localpeer.impl.entity.ContainerHostEntity;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.template.api.TemplateManager;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.resource.ByteValueResource;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    Template template;
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


    class LocalPeerImplForTest extends LocalPeerImpl
    {
        public LocalPeerImplForTest( final DaoManager daoManager, final TemplateManager templateManager,
                                     final CommandExecutor commandExecutor, final HostRegistry hostRegistry,
                                     final Monitor monitor, final SecurityManager securityManager )
        {
            super( daoManager, templateManager, commandExecutor, hostRegistry, monitor, securityManager );
        }
    }


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
        localPeer = spy( new LocalPeerImplForTest( daoManager, templateRegistry, commandExecutor, hostRegistry, monitor,
                securityManager ) );
        localPeer.setIdentityManager( identityManager );
        localPeer.setRelationManager( relationManager );

        localPeer.peerInfo = peerInfo;
        localPeer.resourceHostDataService = resourceHostDataService;
        localPeer.resourceHosts = Sets.newHashSet( ( ResourceHost ) resourceHost );
        localPeer.commandUtil = commandUtil;
        localPeer.exceptionUtil = exceptionUtil;
        localPeer.requestListeners = Sets.newHashSet( requestListener );

        when( daoManager.getEntityManagerFactory() ).thenReturn( entityManagerFactory );
        when( managementHost.getId() ).thenReturn( MANAGEMENT_HOST_ID );

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
        when( templateRegistry.getTemplateByName( TEMPLATE_NAME ) ).thenReturn( template );
        when( template.getName() ).thenReturn( TEMPLATE_NAME );
        when( resourceHost.isConnected() ).thenReturn( true );
        when( hostRegistry.getContainerHostInfoById( CONTAINER_HOST_ID ) ).thenReturn( containerHostInfo );
        when( hostRegistry.getHostInfoById( CONTAINER_HOST_ID ) ).thenReturn( containerHostInfo );
        when( containerHostInfo.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerHost.getHostname() ).thenReturn( CONTAINER_HOST_NAME );
        when( environmentId.getId() ).thenReturn( ENVIRONMENT_ID );
        when( containerHost.getEnvironmentId() ).thenReturn( environmentId );
        doReturn( resourceHost ).when( localPeer ).getResourceHostByHostName( RESOURCE_HOST_NAME );
        doReturn( containerHost ).when( resourceHost ).getContainerHostByHostName( CONTAINER_HOST_NAME );
        when( containerHost.getParent() ).thenReturn( resourceHost );
        when( containerHostInfo.getState() ).thenReturn( ContainerHostState.RUNNING );
        when( containerHost.isConnected() ).thenReturn( true );
        when( resourceHost.getContainerHosts() ).thenReturn( Sets.<ContainerHost>newHashSet( containerHost ) );
        when( requestListener.getRecipient() ).thenReturn( RECIPIENT );
        doReturn( RESPONSE ).when( requestListener ).onRequest( any( Payload.class ) );

        peerMap = new HashMap<>();
        peerMap.put( IP, P2P_IP );
        when( environmentId.getId() ).thenReturn( ENV_ID );

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
    public void testGetContainerHostState() throws Exception
    {
        localPeer.getContainerState( containerHost.getContainerId() );

        verify( containerHostInfo ).getState();
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetContainerHostByName() throws Exception
    {
        assertEquals( containerHost, localPeer.getContainerHostByHostName( CONTAINER_HOST_NAME ) );

        doThrow( new HostNotFoundException( "" ) ).when( resourceHost )
                                                  .getContainerHostByHostName( CONTAINER_HOST_NAME );

        localPeer.getContainerHostByHostName( CONTAINER_HOST_NAME );
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
        assertEquals( resourceHost, localPeer.getResourceHostByHostName( RESOURCE_HOST_NAME ) );

        localPeer.getResourceHostByHostName( "DUMMY NAME" );
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
        assertEquals( resourceHost, localPeer.getResourceHostByContainerHostName( CONTAINER_HOST_NAME ) );
    }


    @Test
    public void testGetResourceHostByContainerId() throws Exception
    {
        assertEquals( resourceHost, localPeer.getResourceHostByContainerId( CONTAINER_HOST_ID ) );
    }


    @Test
    public void testBindHost() throws Exception
    {

        assertEquals( resourceHost, localPeer.findHost( RESOURCE_HOST_ID ) );

        assertEquals( containerHost, localPeer.findHost( CONTAINER_HOST_ID ) );
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


    @Test
    public void testIsConnected() throws Exception
    {
        doReturn( resourceHostInfo ).when( hostRegistry ).getResourceHostByContainerHost( containerHostInfo );
        doReturn( true ).when( hostRegistry ).pingHost( anyString() );
        assertTrue( localPeer.isConnected( containerHost.getContainerId() ) );

        when( hostRegistry.getHostInfoById( CONTAINER_HOST_ID ) ).thenReturn( hostInfo );

        doReturn( "" ).when( hostInfo ).getId();

        assertFalse( localPeer.isConnected( containerHost.getContainerId() ) );

        HostDisconnectedException hostDisconnectedException = mock( HostDisconnectedException.class );

        doThrow( hostDisconnectedException ).when( hostRegistry ).getHostInfoById( CONTAINER_HOST_ID );

        assertFalse( localPeer.isConnected( containerHost.getContainerId() ) );
    }


    @Test( expected = PeerException.class )
    @Ignore
    public void testGetQuotaInfo() throws Exception
    {
        try
        {
            localPeer.getQuota( containerId );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test( expected = PeerException.class )
    @Ignore
    public void testSetQuota() throws Exception
    {
        ContainerQuota quotaInfo = mock( ContainerQuota.class );

        try
        {
            localPeer.setQuota( containerId, quotaInfo );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test( expected = HostNotFoundException.class )
    public void testGetManagementHost() throws Exception
    {

        doThrow( new HostNotFoundException( "" ) ).when( localPeer )
                                                  .getResourceHostByContainerName( Common.MANAGEMENT_HOSTNAME );

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
        doReturn( IP ).when( resourceHostInfo ).getAddress();
        doReturn( managementHost ).when( localPeer ).getManagementHost();

        localPeer.initialized = true;
        localPeer.onHeartbeat( resourceHostInfo, Sets.newHashSet( quotaAlertValue ) );

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
}
