package io.subutai.core.environment.impl;


import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.HostInfo;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.Criteria;
import io.subutai.common.protocol.PlacementStrategy;
import io.subutai.common.protocol.Template;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.api.exception.EnvironmentSecurityException;
import io.subutai.core.environment.impl.builder.EnvironmentBuilder;
import io.subutai.core.environment.impl.dao.BlueprintDataService;
import io.subutai.core.environment.impl.dao.EnvironmentContainerDataService;
import io.subutai.core.environment.impl.dao.EnvironmentDataService;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.exception.EnvironmentTunnelException;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.User;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.tracker.api.Tracker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerImplTest
{
    @Mock
    TemplateRegistry templateRegistry;
    @Mock
    PeerManager peerManager;
    @Mock
    NetworkManager networkManager;
    @Mock
    DaoManager daoManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    Tracker tracker;
    @Mock
    EnvironmentDataService environmentDataService;
    @Mock
    EnvironmentContainerDataService environmentContainerDataService;
    @Mock
    BlueprintDataService blueprintDataService;
    @Mock
    Blueprint blueprint;
    @Mock
    EnvironmentImpl environment;
    @Mock
    User user;
    @Mock
    EnvironmentContainerImpl environmentContainer;
    @Mock
    Peer peer;
    @Mock
    HostInfo hostInfo;
    @Mock
    RuntimeException exception;
    @Mock
    Topology topology;
    @Mock
    NodeGroup nodeGroup;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    LocalPeer localPeer;
    @Mock
    ManagementHost managementHost;
    @Mock
    PeerInfo peerInfo;
    @Mock
    Template template;
    @Mock
    PlacementStrategy placementStrategy;
    @Mock
    Gateway gateway;
    @Mock
    Vni vni;
    @Mock
    EnvironmentBuilder environmentBuilder;
    @Mock
    EnvironmentEventListener listener;
    @Mock
    Set<EnvironmentEventListener> listeners;
    @Mock
    ExecutorService executor;


    EnvironmentManagerImpl environmentManager;


    @Before
    public void setUp() throws Exception
    {
        environmentManager = new EnvironmentManagerImpl( templateRegistry, peerManager, networkManager, daoManager,
                TestUtil.DEFAULT_DOMAIN, identityManager, tracker );
        environmentManager.init();
        environmentManager.environmentContainerDataService = environmentContainerDataService;
        environmentManager.blueprintDataService = blueprintDataService;
        environmentManager.environmentDataService = environmentDataService;
        when( environmentDataService.find( anyString() ) ).thenReturn( environment );
        when( identityManager.getUser() ).thenReturn( user );
        when( user.isAdmin() ).thenReturn( true );
        when( user.getId() ).thenReturn( TestUtil.USER_ID );
        when( environment.getId() ).thenReturn( TestUtil.ENV_ID );
        when( environment.getSubnetCidr() ).thenReturn( TestUtil.SUBNET );
        when( environment.getUserId() ).thenReturn( TestUtil.USER_ID );
        when( environment.getContainerHostById( TestUtil.CONTAINER_ID ) ).thenReturn( environmentContainer );
        when( environment.getContainerHosts() ).thenReturn( Sets.<ContainerHost>newHashSet( environmentContainer ) );
        when( environmentContainer.getPeer() ).thenReturn( peer );
        when( environmentContainer.getId() ).thenReturn( TestUtil.CONTAINER_ID );
        when( peer.getContainerHostInfoById( TestUtil.CONTAINER_ID ) ).thenReturn( hostInfo );
        when( environmentContainerDataService.find( TestUtil.CONTAINER_ID.toString() ) )
                .thenReturn( environmentContainer );
        when( environmentDataService.getAll() ).thenReturn( Sets.newHashSet( environment ) );
        Map<Peer, Set<NodeGroup>> nodeGroupPlacement = Maps.newHashMap();
        nodeGroupPlacement.put( peer, Sets.newHashSet( nodeGroup ) );
        when( topology.getNodeGroupPlacement() ).thenReturn( nodeGroupPlacement );
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getId() ).thenReturn( TestUtil.LOCAL_PEER_ID );
        when( peer.getId() ).thenReturn( TestUtil.PEER_ID );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( peer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getIp() ).thenReturn( TestUtil.IP );
        when( nodeGroup.getTemplateName() ).thenReturn( TestUtil.TEMPLATE_NAME );
        when( templateRegistry.getTemplate( TestUtil.TEMPLATE_NAME ) ).thenReturn( template );
        when( template.getRemoteClone( any( UUID.class ) ) ).thenReturn( template );
        when( nodeGroup.getContainerPlacementStrategy() ).thenReturn( placementStrategy );
        when( placementStrategy.getStrategyId() ).thenReturn( "ROUND-ROBIN" );
        when( placementStrategy.getCriteriaAsList() ).thenReturn( Lists.<Criteria>newArrayList() );
        when( environmentContainer.getEnvironmentId() ).thenReturn( TestUtil.ENV_ID.toString() );
        when( vni.getVni() ).thenReturn( TestUtil.VNI );
    }


    private void throwEnvironmentNotFoundException()
    {
        when( environmentDataService.find( anyString() ) ).thenReturn( null );
    }


    private void setUserAsAdmin( boolean isAdmin )
    {
        when( user.isAdmin() ).thenReturn( isAdmin );
    }


    private void setEnvironmentUserId( Long userId )
    {
        when( environment.getUserId() ).thenReturn( userId );
    }


    @Test
    public void testSaveBlueprint() throws Exception
    {
        environmentManager.saveBlueprint( blueprint );

        verify( blueprintDataService ).persist( blueprint );
    }


    @Test
    public void testRemoveBlueprint() throws Exception
    {
        environmentManager.removeBlueprint( TestUtil.BLUEPRINT_ID );

        verify( blueprintDataService ).remove( TestUtil.BLUEPRINT_ID );
    }


    @Test
    public void testGetBlueprints() throws Exception
    {
        environmentManager.getBlueprints();

        verify( blueprintDataService ).getAll();
    }


    @Test
    public void testGetDefaultDomainName() throws Exception
    {
        assertEquals( TestUtil.DEFAULT_DOMAIN, environmentManager.getDefaultDomainName() );
    }


    @Test( expected = EnvironmentManagerException.class )
    public void testUpdateContainersMetadata() throws Exception
    {

        environmentManager.updateEnvironmentContainersMetadata( TestUtil.ENV_ID );

        verify( environmentContainerDataService ).update( environmentContainer );

        throwEnvironmentNotFoundException();

        environmentManager.updateEnvironmentContainersMetadata( TestUtil.ENV_ID );

        doThrow( exception ).when( environmentContainerDataService ).update( environmentContainer );

        environmentManager.updateEnvironmentContainersMetadata( TestUtil.ENV_ID );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetEnvironments() throws Exception
    {
        setUserAsAdmin( false );
        setEnvironmentUserId( 0l );

        environmentManager.getEnvironments();
    }


    @Test
    public void testFindEnvironment() throws Exception
    {
        Environment environment1 = environmentManager.findEnvironment( TestUtil.ENV_ID, true );

        assertEquals( environment, environment1 );

        environment1 = environmentManager.findEnvironment( TestUtil.ENV_ID );

        assertEquals( environment, environment1 );
    }


    @Test
    public void testCreateEmptyEnvironment() throws Exception
    {
        Environment environment1 =
                environmentManager.createEmptyEnvironment( TestUtil.ENV_NAME, TestUtil.SUBNET, TestUtil.SSH_KEY );

        assertEquals( EnvironmentStatus.EMPTY, environment1.getStatus() );
    }


    @Test( expected = EnvironmentCreationException.class )
    public void testCreateEnvironment() throws Exception
    {
        environmentManager.createEnvironment( TestUtil.ENV_NAME, topology, TestUtil.SUBNET, TestUtil.SSH_KEY, false );

        verify( environmentDataService, times( 2 ) ).find( anyString() );

        doThrow( new PeerException( "" ) ).when( managementHost ).createGateway( anyString(), anyInt() );

        environmentManager.createEnvironment( TestUtil.ENV_NAME, topology, TestUtil.SUBNET, TestUtil.SSH_KEY, false );
    }


    @Test( expected = EnvironmentDestructionException.class )
    public void testDestroyEnvironment() throws Exception
    {
        environmentManager.destroyEnvironment( TestUtil.ENV_ID, false, false, true, trackerOperation );

        verify( peerManager ).getLocalPeer();

        environmentManager.destroyEnvironment( TestUtil.ENV_ID, false, false );

        verify( peerManager, times( 2 ) ).getLocalPeer();

        when( environment.getStatus() ).thenReturn( EnvironmentStatus.UNDER_MODIFICATION );

        environmentManager.destroyEnvironment( TestUtil.ENV_ID, false, false, true, trackerOperation );
    }


    @Test( expected = EnvironmentModificationException.class )
    public void testGrowEnvironment() throws Exception
    {
        environmentManager.growEnvironment( TestUtil.ENV_ID, topology, false, true, trackerOperation );

        verify( trackerOperation ).addLogDone( anyString() );

        environmentManager.growEnvironment( TestUtil.ENV_ID, topology, false );

        verify( trackerOperation, times( 2 ) ).addLogDone( anyString() );

        when( environment.getStatus() ).thenReturn( EnvironmentStatus.UNDER_MODIFICATION );

        environmentManager.growEnvironment( TestUtil.ENV_ID, topology, false );
    }


    @Test( expected = EnvironmentModificationException.class )
    public void testDestroyContainer() throws Exception
    {
        environmentManager.destroyContainer( environmentContainer, false, false );

        verify( environmentContainer ).destroy();

        when( environment.getStatus() ).thenReturn( EnvironmentStatus.UNDER_MODIFICATION );

        try
        {
            environmentManager.destroyContainer( environmentContainer, false, false );
            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        when( environment.getStatus() ).thenReturn( EnvironmentStatus.HEALTHY );

        doThrow( new ContainerHostNotFoundException( null ) ).when( environment )
                                                             .getContainerHostById( any( UUID.class ) );

        environmentManager.destroyContainer( environmentContainer, false, false );
    }


    @Test
    public void testSetSshKey() throws Exception
    {
        environmentManager.setSshKey( TestUtil.ENV_ID, TestUtil.SSH_KEY, false );

        verify( trackerOperation ).addLogDone( anyString() );
    }


    @Test
    public void testRemoveEnvironment() throws Exception
    {
        environmentManager.removeEnvironment( TestUtil.ENV_ID );

        verify( environmentDataService ).remove( TestUtil.ENV_ID.toString() );
    }


    @Test( expected = EnvironmentTunnelException.class )
    public void testSetupEnvironmentTunnel() throws Exception
    {
        environmentManager.setupEnvironmentTunnel( TestUtil.ENV_ID, Sets.newHashSet( peer ) );

        verify( peer ).importCertificate( anyString(), anyString() );

        doThrow( new PeerException( "" ) ).when( localPeer ).exportEnvironmentCertificate( TestUtil.ENV_ID );

        environmentManager.setupEnvironmentTunnel( TestUtil.ENV_ID, Sets.newHashSet( peer ) );
    }


    @Test( expected = EnvironmentManagerException.class )
    public void testGetUsedGateways() throws Exception
    {
        when( peer.getGateways() ).thenReturn( Sets.newHashSet( gateway ) );

        Map<Peer, Set<Gateway>> gateways = environmentManager.getUsedGateways( Sets.newHashSet( peer ) );

        assertTrue( gateways.get( peer ).contains( gateway ) );

        doThrow( new PeerException( "" ) ).when( peer ).getGateways();

        environmentManager.getUsedGateways( Sets.newHashSet( peer ) );
    }


    @Test( expected = EnvironmentManagerException.class )
    public void testFindFreeVni() throws Exception
    {
        when( peer.getReservedVnis() ).thenReturn( Sets.newHashSet( vni ) );

        Long vni = environmentManager.findFreeVni( Sets.newHashSet( peer ) );

        assertNotEquals( TestUtil.VNI, vni );


        doThrow( new PeerException( "" ) ).when( peer ).getReservedVnis();

        environmentManager.findFreeVni( Sets.newHashSet( peer ) );
    }


    @Test
    public void testSaveEnvironment() throws Exception
    {
        environmentManager.saveEnvironment( environment );

        verify( environmentDataService ).persist( environment );
    }


    @Test
    public void testBuild() throws Exception
    {
        environmentManager.environmentBuilder = environmentBuilder;

        environmentManager.build( environment, topology );

        verify( environmentBuilder ).build( environment, topology );
    }


    @Test
    public void testSetEnvironmentTransientFields() throws Exception
    {
        environmentManager.setEnvironmentTransientFields( environment );

        verify( environment ).setDataService( environmentDataService );
    }


    @Test
    public void testSetContainersTransientFields() throws Exception
    {
        environmentManager.setContainersTransientFields( Sets.<ContainerHost>newHashSet( environmentContainer ) );

        verify( environmentContainer ).setEnvironmentManager( environmentManager );
    }


    @Test
    public void testConfigureSsh() throws Exception
    {
        when( environmentContainer.getSshGroupId() ).thenReturn( TestUtil.SSH_GROUP_ID );

        environmentManager.configureSsh( Sets.<ContainerHost>newHashSet( environmentContainer ) );

        verify( networkManager ).exchangeSshKeys( Sets.<ContainerHost>newHashSet( environmentContainer ) );
    }


    @Test
    public void testConfigureHosts() throws Exception
    {
        when( environmentContainer.getHostsGroupId() ).thenReturn( TestUtil.HOSTS_GROUP_ID );
        when( environmentContainer.getDomainName() ).thenReturn( TestUtil.DEFAULT_DOMAIN );

        environmentManager.configureHosts( Sets.<ContainerHost>newHashSet( environmentContainer ) );

        verify( networkManager )
                .registerHosts( Sets.<ContainerHost>newHashSet( environmentContainer ), TestUtil.DEFAULT_DOMAIN );
    }


    @Test
    public void testAddListener() throws Exception
    {
        environmentManager.listeners = listeners;

        environmentManager.registerListener( listener );

        verify( listeners ).add( listener );
    }


    @Test
    public void testRemoveListener() throws Exception
    {
        environmentManager.listeners = listeners;

        environmentManager.unregisterListener( listener );

        verify( listeners ).remove( listener );
    }


    @Test
    public void testDispose() throws Exception
    {
        environmentManager.executor = executor;

        environmentManager.dispose();

        verify( executor ).shutdown();
    }


    @Test( expected = EnvironmentSecurityException.class )
    public void testGetUser() throws Exception
    {
        User user = environmentManager.getUser();

        assertEquals( this.user, user );

        when( identityManager.getUser() ).thenReturn( null );

        environmentManager.getUser();
    }


    @Test( expected = EnvironmentSecurityException.class )
    public void testCheckAccess() throws Exception
    {
        when( environment.getUserId() ).thenReturn( 0l );
        when( user.isAdmin() ).thenReturn( false );

        environmentManager.checkAccess( environment );
    }
}
