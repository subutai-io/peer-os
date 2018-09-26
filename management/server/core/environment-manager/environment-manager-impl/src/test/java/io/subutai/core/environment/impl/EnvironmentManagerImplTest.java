package io.subutai.core.environment.impl;


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.security.auth.Subject;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentCreationRef;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentPeer;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Nodes;
import io.subutai.common.environment.Topology;
import io.subutai.common.metric.Alert;
import io.subutai.common.metric.AlertValue;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.core.environment.impl.adapter.BazaarEnvironment;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.ContainerDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.destruction.EnvironmentDestructionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.EnvironmentModifyWorkflow;
import io.subutai.core.environment.impl.workflow.modification.HostnameModificationWorkflow;
import io.subutai.core.environment.impl.workflow.modification.P2PSecretKeyModificationWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyAdditionWorkflow;
import io.subutai.core.environment.impl.workflow.modification.SshKeyRemovalWorkflow;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.peer.api.PeerAction;
import io.subutai.core.peer.api.PeerActionResponse;
import io.subutai.core.peer.api.PeerActionType;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.template.api.TemplateManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.bazaar.share.common.BazaaarAdapter;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerImplTest
{

    private static final int PORT = 80;
    EnvironmentManagerImpl environmentManager;

    @Mock
    PeerManager peerManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    SecurityManager securityManager;
    @Mock
    RelationManager relationManager;
    @Mock
    BazaaarAdapter bazaaarAdapter;
    @Mock
    EnvironmentService environmentService;
    @Mock
    Tracker tracker;
    @Mock
    EnvironmentAdapter environmentAdapter;
    @Mock
    ScheduledExecutorService scheduledExecutor;
    @Mock
    ExecutorService cachedExecutor;
    @Mock
    Session session;

    Subject systemUser = new Subject();
    @Mock
    User user;

    @Mock
    CancellableWorkflow checkWorkflow;

    LocalEnvironment environment = TestHelper.ENVIRONMENT();

    @Mock
    EnvironmentPeer environmentPeer;
    LocalPeer localPeer = TestHelper.LOCAL_PEER();
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();
    @Mock
    Topology topology;
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();
    Node node = TestHelper.NODE();
    @Mock
    PeerException peerException;
    @Mock
    JsonUtil jsonUtil;
    @Mock
    UserDelegate userDelegate;
    @Mock
    EnvironmentCreationRef environmentCreationRef;
    @Mock
    Map<String, CancellableWorkflow> activeWorkflows;
    @Mock
    BazaarEnvironment bzrEnvironment;
    @Mock
    KeyManager keyManager;
    @Mock
    PGPKeyUtil pgpKeyUtil;
    @Mock
    TemplateManager templateManager;
    @Mock
    SystemManager systemManager;


    class EnvironmentManagerImplSUT extends EnvironmentManagerImpl
    {
        public EnvironmentManagerImplSUT( final TemplateManager templateManager, final PeerManager peerManager,
                                          final SecurityManager securityManager, final IdentityManager identityManager,
                                          final Tracker tracker, final RelationManager relationManager,
                                          final BazaaarAdapter bazaaarAdapter, final EnvironmentService environmentService,
                                          final SystemManager systemManager )
        {
            super( templateManager, peerManager, securityManager, identityManager, tracker, relationManager,
                    bazaaarAdapter,
                    environmentService, systemManager );
        }


        protected EnvironmentAdapter getEnvironmentAdapter( BazaaarAdapter bazaaarAdapter )
        {
            return environmentAdapter;
        }


        protected ScheduledExecutorService getScheduleExecutor()
        {
            return scheduledExecutor;
        }


        protected ExecutorService getCachedExecutor()
        {
            return cachedExecutor;
        }
    }


    @Before
    public void setUp() throws Exception
    {
        doReturn( session ).when( identityManager ).loginSystemUser();
        doReturn( systemUser ).when( session ).getSubject();

        environmentManager =
                spy( new EnvironmentManagerImplSUT( templateManager, peerManager, securityManager, identityManager,
                        tracker, relationManager, bazaaarAdapter, environmentService, systemManager ) );
        environmentManager.jsonUtil = jsonUtil;
        environmentManager.pgpKeyUtil = pgpKeyUtil;
        environmentManager.activeWorkflows = activeWorkflows;

        doReturn( Sets.newHashSet( environment ) ).when( environmentService ).getAll();
        doReturn( environment ).when( environmentService ).find( TestHelper.ENV_ID );
        doReturn( environmentContainer ).when( environmentService ).mergeContainer( environmentContainer );
        doReturn( Sets.newHashSet( environmentPeer ) ).when( environment ).getEnvironmentPeers();
        doReturn( TestHelper.PEER_ID ).when( environmentPeer ).getPeerId();
        doReturn( Lists.newArrayList( environmentContainer ) ).when( localPeer )
                                                              .getPeerContainers( TestHelper.PEER_ID );
        doReturn( localPeer ).when( peerManager ).getLocalPeer();
        doReturn( Sets.newHashSet( TestHelper.PEER_ID ) ).when( topology ).getAllPeers();
        doReturn( localPeer ).when( peerManager ).getPeer( TestHelper.PEER_ID );
        doReturn( Sets.newHashSet( localPeer ) ).when( environment ).getPeers();

        Map<String, Set<Node>> nodeGroupPlacement = Maps.newHashMap();
        nodeGroupPlacement.put( TestHelper.PEER_ID, Sets.newHashSet( node ) );
        doReturn( nodeGroupPlacement ).when( topology ).getNodeGroupPlacement();
        doReturn( TestHelper.ENV_NAME ).when( topology ).getEnvironmentName();
        doReturn( TestHelper.SSH_KEY ).when( topology ).getSshKey();

        doReturn( true ).when( localPeer ).isOnline();
        doReturn( true ).when( localPeer ).canAccommodate( any( Nodes.class ) );
        doReturn( trackerOperation ).when( tracker )
                                    .createTrackerOperation( eq( environmentManager.MODULE_NAME ), anyString() );
        doReturn( user ).when( identityManager ).getActiveUser();
        doReturn( TestHelper.USER_ID ).when( user ).getId();
        doReturn( userDelegate ).when( identityManager ).getUserDelegate( TestHelper.USER_ID );
        doReturn( TestHelper.USER_ID.toString() ).when( userDelegate ).getId();
        doReturn( environment ).when( environmentManager ).loadEnvironment( TestHelper.ENV_ID );

        doReturn( environmentContainer ).when( environment ).getContainerHostById( TestHelper.CONTAINER_ID );
        doReturn( keyManager ).when( securityManager ).getKeyManager();
        doReturn( environment ).when( environmentContainer ).getEnvironment();
    }


    @Test
    public void testDispose() throws Exception
    {
        doReturn( Sets.newHashSet( checkWorkflow ) ).when( activeWorkflows ).values();

        environmentManager.dispose();

        verify( cachedExecutor ).shutdown();
        verify( scheduledExecutor ).shutdown();
        verify( checkWorkflow ).cancel();
    }


    @Test
    public void testOnPeerAction() throws Exception
    {
        PeerAction peerAction = mock( PeerAction.class );
        doReturn( PeerActionType.REGISTER ).when( peerAction ).getType();

        PeerActionResponse response = environmentManager.onPeerAction( peerAction );

        assertEquals( PeerActionResponse.Ok().getType(), response.getType() );

        //--------------------------------------------

        doReturn( PeerActionType.UNREGISTER ).when( peerAction ).getType();
        doReturn( true ).when( environmentManager ).isPeerInUse( anyString() );

        response = environmentManager.onPeerAction( peerAction );

        assertEquals( PeerActionResponse.Fail().getType(), response.getType() );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertEquals( EnvironmentManagerImpl.MODULE_NAME, environmentManager.getName() );
    }


    @Test
    public void testIsPeerInUse() throws Exception
    {
        assertTrue( environmentManager.isPeerInUse( TestHelper.PEER_ID ) );

        doReturn( Sets.newHashSet() ).when( environment ).getEnvironmentPeers();

        assertTrue( environmentManager.isPeerInUse( TestHelper.PEER_ID ) );

        doReturn( EnvironmentStatus.UNDER_MODIFICATION ).when( environment ).getStatus();

        assertTrue( environmentManager.isPeerInUse( TestHelper.PEER_ID ) );
    }


    @Test
    public void testGetPeers() throws Exception
    {
        Set<Peer> peerSet = environmentManager.getPeers( topology );

        assertTrue( peerSet.contains( localPeer ) );
    }


    @Test
    public void testGetEnvironments() throws Exception
    {
        Set<Environment> environments = environmentManager.getEnvironments();

        assertTrue( environments.contains( environment ) );
    }


    @Test
    public void testSetTransientFields() throws Exception
    {
        doReturn( Sets.newHashSet( environmentContainer ) ).when( environment ).getContainerHosts();
        environmentManager.setTransientFields( Sets.<Environment>newHashSet( environment ) );

        verify( environmentManager ).setEnvironmentTransientFields( environment );
        verify( environmentManager ).setContainersTransientFields( environment );
    }


    @Test
    public void testGetEnvironmentsByOwnerId() throws Exception
    {
        Set<Environment> environments = environmentManager.getEnvironmentsByOwnerId( TestHelper.USER_ID );

        assertTrue( environments.contains( environment ) );
    }


    @Test
    public void testCreateEnvironment() throws Exception
    {
        doReturn( environment ).when( environmentManager ).createEmptyEnvironment( topology );
        EnvironmentCreationWorkflow environmentCreationWorkflow = mock( EnvironmentCreationWorkflow.class );

        doReturn( environmentCreationWorkflow ).when( environmentManager )
                                               .getEnvironmentCreationWorkflow( environment, topology,
                                                       TestHelper.SSH_KEY, trackerOperation );

        environmentManager.createEnvironment( topology, false, trackerOperation );

        verify( environmentCreationWorkflow ).join();

        //--------------------

        environmentManager.removeActiveWorkflow( TestHelper.ENV_ID );
        doReturn( true ).when( environmentCreationWorkflow ).isFailed();

        try
        {
            environmentManager.createEnvironment( topology, false, trackerOperation );

            fail( "Expected EnvironmentCreationException" );
        }
        catch ( EnvironmentCreationException e )
        {
        }


        //--------------------

        doReturn( false ).when( localPeer ).isOnline();

        try
        {
            environmentManager.createEnvironment( topology, false, trackerOperation );

            fail( "Expected EnvironmentCreationException" );
        }
        catch ( EnvironmentCreationException e )
        {
        }

        verify( trackerOperation ).addLogFailed( anyString() );

        //--------------------

        reset( environmentCreationWorkflow );
        doThrow( peerException ).when( environmentManager ).getPeers( topology );

        try
        {
            environmentManager.createEnvironment( topology, false, trackerOperation );

            fail( "Expected EnvironmentCreationException" );
        }
        catch ( EnvironmentCreationException e )
        {
        }
    }


    @Test
    public void testCreateEnvironment2() throws Exception
    {
        doReturn( UUID.randomUUID() ).when( trackerOperation ).getId();
        doReturn( environment ).when( environmentManager ).createEnvironment( topology, false, trackerOperation );

        assertNotNull( environmentManager.createEnvironment( topology, false ) );
    }


    @Test
    public void testCreateEmptyEnvironment() throws Exception
    {
        PGPSecretKeyRing secretKeyRing = mock( PGPSecretKeyRing.class );
        doReturn( secretKeyRing ).when( environmentManager ).createEnvironmentKeyPair( any( EnvironmentId.class ) );

        environmentManager.createEmptyEnvironment( topology );

        verify( environmentManager ).save( any( LocalEnvironment.class ) );
    }


    @Test
    public void testGrowEnvironment() throws Exception
    {

        doReturn( environmentCreationRef ).when( environmentManager )
                                          .modifyEnvironment( eq( TestHelper.ENV_ID ), eq( topology ), anySet(),
                                                  anyMap(), anyBoolean() );

        environmentManager.growEnvironment( TestHelper.ENV_ID, topology, false );

        verify( environmentManager )
                .modifyEnvironment( eq( TestHelper.ENV_ID ), eq( topology ), anySet(), anyMap(), anyBoolean() );
    }


    @Test
    public void testModifyEnvironment() throws Exception
    {
        Set<String> removedContainers = Sets.newHashSet( TestHelper.CONTAINER_ID );
        Map<String, ContainerQuota> changedContainers = Maps.newHashMap();
        changedContainers.put( TestHelper.CONTAINER_ID, new ContainerQuota( ContainerSize.LARGE ) );
        EnvironmentModifyWorkflow environmentModifyWorkflow = mock( EnvironmentModifyWorkflow.class );
        doReturn( environmentModifyWorkflow ).when( environmentManager )
                                             .getEnvironmentModifyingWorkflow( environment, topology, trackerOperation,
                                                     removedContainers, changedContainers );
        doNothing().when( environmentManager ).registerActiveWorkflow( environment, environmentModifyWorkflow );

        assertNotNull( environmentManager
                .modifyEnvironment( TestHelper.ENV_ID, topology, removedContainers, changedContainers, false ) );

        //-----

        doReturn( true ).when( environmentModifyWorkflow ).isFailed();

        try
        {

            environmentManager
                    .modifyEnvironment( TestHelper.ENV_ID, topology, removedContainers, changedContainers, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        //-----

        doReturn( EnvironmentStatus.UNDER_MODIFICATION ).when( environment ).getStatus();

        try
        {
            environmentManager
                    .modifyEnvironment( TestHelper.ENV_ID, topology, removedContainers, changedContainers, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }
        verify( trackerOperation ).addLogFailed( anyString() );

        //-----

        doReturn( false ).when( localPeer ).isOnline();

        try
        {
            environmentManager
                    .modifyEnvironment( TestHelper.ENV_ID, topology, removedContainers, changedContainers, false );
            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        verify( trackerOperation, times( 2 ) ).addLogFailed( anyString() );

        //-----

        doThrow( new PeerException() ).when( environment ).getPeers();

        try
        {
            environmentManager
                    .modifyEnvironment( TestHelper.ENV_ID, topology, removedContainers, changedContainers, false );
            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        verify( trackerOperation, times( 3 ) ).addLogFailed( anyString() );
    }


    @Test
    public void testAddSshKey() throws Exception
    {
        SshKeyAdditionWorkflow sshKeyAdditionWorkflow = mock( SshKeyAdditionWorkflow.class );
        doNothing().when( environmentManager ).registerActiveWorkflow( environment, sshKeyAdditionWorkflow );
        doReturn( sshKeyAdditionWorkflow ).when( environmentManager )
                                          .getSshKeyAdditionWorkflow( environment, TestHelper.SSH_KEY,
                                                  trackerOperation );

        environmentManager.addSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, false );

        verify( environmentAdapter ).addSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY );

        //-----

        doReturn( true ).when( sshKeyAdditionWorkflow ).isFailed();

        try
        {

            environmentManager.addSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        //-----

        doReturn( EnvironmentStatus.UNDER_MODIFICATION ).when( environment ).getStatus();

        try
        {

            environmentManager.addSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }
    }


    @Test
    public void testRemoveSshKey() throws Exception
    {
        SshKeyRemovalWorkflow sshKeyRemovalWorkflow = mock( SshKeyRemovalWorkflow.class );
        doNothing().when( environmentManager ).registerActiveWorkflow( environment, sshKeyRemovalWorkflow );
        doReturn( sshKeyRemovalWorkflow ).when( environmentManager )
                                         .getSshKeyRemovalWorkflow( environment, TestHelper.SSH_KEY, trackerOperation );

        environmentManager.removeSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, false );

        verify( environmentAdapter ).removeSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY );

        //-----

        doReturn( true ).when( sshKeyRemovalWorkflow ).isFailed();

        try
        {

            environmentManager.removeSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        //-----

        doReturn( EnvironmentStatus.UNDER_MODIFICATION ).when( environment ).getStatus();

        try
        {

            environmentManager.removeSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }
    }


    @Test
    public void testGetSshKeys() throws Exception
    {
        SshKeys keys = mock( SshKeys.class );
        doReturn( keys ).when( localPeer ).getSshKeys( TestHelper.ENVIRONMENT_ID, SshEncryptionType.DSA );
        SshKey sshKey = mock( SshKey.class );
        doReturn( Sets.newHashSet( sshKey ) ).when( keys ).getKeys();

        environmentManager.getSshKeys( TestHelper.ENV_ID, SshEncryptionType.DSA );

        verify( keys ).getKeys();
    }


    @Test
    public void testCreateSshKey() throws Exception
    {
        doReturn( environmentContainer ).when( environment ).getContainerHostByHostname( TestHelper.HOSTNAME );
        doReturn( localPeer ).when( environmentContainer ).getPeer();
        SshKey sshKey = mock( SshKey.class );
        doReturn( sshKey ).when( localPeer )
                          .createSshKey( TestHelper.ENVIRONMENT_ID, TestHelper.CONT_HOST_ID, SshEncryptionType.ECDSA );

        environmentManager.createSshKey( TestHelper.ENV_ID, TestHelper.HOSTNAME, SshEncryptionType.ECDSA );

        verify( localPeer ).createSshKey( TestHelper.ENVIRONMENT_ID, TestHelper.CONT_HOST_ID, SshEncryptionType.ECDSA );
    }


    @Test
    public void testResetP2PSecretKey() throws Exception
    {
        P2PSecretKeyModificationWorkflow p2PSecretKeyModificationWorkflow =
                mock( P2PSecretKeyModificationWorkflow.class );
        doNothing().when( environmentManager ).registerActiveWorkflow( environment, p2PSecretKeyModificationWorkflow );
        doReturn( p2PSecretKeyModificationWorkflow ).when( environmentManager )
                                                    .getP2PSecretKeyModificationWorkflow( environment, "SECRET", 123L,
                                                            trackerOperation );

        environmentManager.resetP2PSecretKey( TestHelper.ENV_ID, "SECRET", 123L, false );

        verify( p2PSecretKeyModificationWorkflow ).join();

        //-----

        doReturn( true ).when( p2PSecretKeyModificationWorkflow ).isFailed();

        try
        {
            environmentManager.resetP2PSecretKey( TestHelper.ENV_ID, "SECRET", 123L, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        //-----

        doReturn( EnvironmentStatus.UNDER_MODIFICATION ).when( environment ).getStatus();

        try
        {
            environmentManager.resetP2PSecretKey( TestHelper.ENV_ID, "SECRET", 123L, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }
    }


    @Test
    public void testDestroyEnvironment() throws Exception
    {
        EnvironmentDestructionWorkflow environmentDestructionWorkflow = mock( EnvironmentDestructionWorkflow.class );
        doReturn( environmentDestructionWorkflow ).when( environmentManager )
                                                  .getEnvironmentDestructionWorkflow( environment, trackerOperation );
        doNothing().when( environmentManager ).registerActiveWorkflow( environment, environmentDestructionWorkflow );

        environmentManager.destroyEnvironment( TestHelper.ENV_ID, false );

        verify( environmentDestructionWorkflow ).join();

        //-----

        doReturn( true ).when( environmentDestructionWorkflow ).isFailed();

        try
        {
            environmentManager.destroyEnvironment( TestHelper.ENV_ID, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentDestructionException e )
        {
        }

        //-----

        doReturn( EnvironmentStatus.UNDER_MODIFICATION ).when( environment ).getStatus();

        try
        {
            environmentManager.destroyEnvironment( TestHelper.ENV_ID, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentDestructionException e )
        {
        }

        //-----

        doReturn( bzrEnvironment ).when( environmentManager ).loadEnvironment( TestHelper.ENV_ID );

        environmentManager.destroyEnvironment( TestHelper.ENV_ID, false );

        verify( environmentAdapter ).removeEnvironment( bzrEnvironment );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {
        ContainerDestructionWorkflow containerDestructionWorkflow = mock( ContainerDestructionWorkflow.class );
        doReturn( containerDestructionWorkflow ).when( environmentManager )
                                                .getContainerDestructionWorkflow( environment, environmentContainer,
                                                        trackerOperation );
        doNothing().when( environmentManager ).registerActiveWorkflow( environment, containerDestructionWorkflow );

        environmentManager.destroyContainer( TestHelper.ENV_ID, TestHelper.CONTAINER_ID, false );

        verify( containerDestructionWorkflow ).join();


        //-----

        doReturn( true ).when( containerDestructionWorkflow ).isFailed();

        try
        {
            environmentManager.destroyContainer( TestHelper.ENV_ID, TestHelper.CONTAINER_ID, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        //-----

        doThrow( new ContainerHostNotFoundException( "" ) ).when( environment )
                                                           .getContainerHostById( TestHelper.CONTAINER_ID );

        try
        {
            environmentManager.destroyContainer( TestHelper.ENV_ID, TestHelper.CONTAINER_ID, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        //-----

        doReturn( EnvironmentStatus.UNDER_MODIFICATION ).when( environment ).getStatus();


        try
        {
            environmentManager.destroyContainer( TestHelper.ENV_ID, TestHelper.CONTAINER_ID, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        //-----

        doReturn( bzrEnvironment ).when( environmentManager ).loadEnvironment( TestHelper.ENV_ID );

        environmentManager.destroyContainer( TestHelper.ENV_ID, TestHelper.CONTAINER_ID, false );

        verify( environmentAdapter ).destroyContainer( bzrEnvironment, TestHelper.CONTAINER_ID );
    }


    @Test
    public void testChangeContainerHostname() throws Exception
    {
        HostnameModificationWorkflow hostnameModificationWorkflow = mock( HostnameModificationWorkflow.class );
        doReturn( hostnameModificationWorkflow ).when( environmentManager )
                                                .getHostnameModificationWorkflow( environment, TestHelper.CONT_HOST_ID,
                                                        "new", trackerOperation );
        doNothing().when( environmentManager ).registerActiveWorkflow( environment, hostnameModificationWorkflow );

        environmentManager.changeContainerHostname( TestHelper.CONT_HOST_ID, "new", false );

        verify( hostnameModificationWorkflow ).join();

        //-----

        doReturn( true ).when( hostnameModificationWorkflow ).isFailed();

        try
        {
            environmentManager.changeContainerHostname( TestHelper.CONT_HOST_ID, "new", false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }
    }


    @Test( expected = IllegalStateException.class )
    public void testRegisterActiveWorkflow() throws Exception
    {
        environmentManager.registerActiveWorkflow( environment, checkWorkflow );

        verify( activeWorkflows ).put( TestHelper.ENV_ID, checkWorkflow );

        doReturn( checkWorkflow ).when( activeWorkflows ).get( TestHelper.ENV_ID );

        environmentManager.registerActiveWorkflow( environment, checkWorkflow );
    }


    @Test
    public void testRemoveActiveWorkflow() throws Exception
    {
        environmentManager.removeActiveWorkflow( TestHelper.ENV_ID );

        verify( activeWorkflows ).remove( TestHelper.ENV_ID );
    }


    @Test
    public void testCancelEnvironmentWorkflow() throws Exception
    {
        doReturn( checkWorkflow ).when( activeWorkflows ).get( TestHelper.ENV_ID );

        environmentManager.cancelEnvironmentWorkflow( TestHelper.ENV_ID );

        verify( environmentManager ).removeActiveWorkflow( TestHelper.ENV_ID );

        //-----

        reset( activeWorkflows );

        doReturn( environment ).when( environmentManager ).update( environment );

        environmentManager.cancelEnvironmentWorkflow( TestHelper.ENV_ID );

        verify( environment ).setStatus( EnvironmentStatus.CANCELLED );
    }


    @Test
    public void getActiveWorkflows() throws Exception
    {
        Map workflows = environmentManager.getActiveWorkflows();

        assertFalse( workflows.isEmpty() );
    }


    @Test
    public void testLoadEnvironment() throws Exception
    {
        ReservedNetworkResources networkResource = mock( ReservedNetworkResources.class );
        doReturn( networkResource ).when( localPeer ).getReservedNetworkResources();
        doCallRealMethod().when( environmentManager ).loadEnvironment( TestHelper.ENV_ID );

        assertNotNull( environmentManager.loadEnvironment( TestHelper.ENV_ID ) );

        //-----

        doReturn( null ).when( environmentService ).find( TestHelper.ENV_ID );

        doReturn( null ).when( environmentManager ).findRemoteEnvironment( TestHelper.ENV_ID );

        try
        {
            environmentManager.loadEnvironment( TestHelper.ENV_ID );

            fail( "Expected EnvironmentNotFoundException" );
        }
        catch ( EnvironmentNotFoundException e )
        {
        }


        doReturn( bzrEnvironment ).when( environmentAdapter ).get( TestHelper.ENV_ID );
        reset( environmentService );

        environmentManager.loadEnvironment( TestHelper.ENV_ID );

        verify( environmentService, never() ).find( TestHelper.ENV_ID );
    }


    @Test
    public void testRemoveEnvironmentDomain() throws Exception
    {
        environmentManager.removeEnvironmentDomain( TestHelper.ENV_ID );

        verify( environmentManager ).modifyEnvironmentDomain( TestHelper.ENV_ID, null, null, null );
    }


    @Test
    public void testAssignEnvironmentDomain() throws Exception
    {
        environmentManager
                .assignEnvironmentDomain( TestHelper.ENV_ID, "new", ProxyLoadBalanceStrategy.STICKY_SESSION, "path" );

        verify( environmentManager )
                .modifyEnvironmentDomain( TestHelper.ENV_ID, "new", ProxyLoadBalanceStrategy.STICKY_SESSION, "path" );
    }


    @Test
    public void testModifyDomain() throws Exception
    {
        environmentManager
                .modifyEnvironmentDomain( TestHelper.ENV_ID, "new", ProxyLoadBalanceStrategy.STICKY_SESSION, "path" );

        verify( localPeer ).setVniDomain( TestHelper.VNI, "new", ProxyLoadBalanceStrategy.STICKY_SESSION, "path" );

        //-----

        environmentManager.modifyEnvironmentDomain( TestHelper.ENV_ID, null, null, null );

        verify( localPeer ).removeVniDomain( TestHelper.VNI );
    }


    @Test( expected = EnvironmentManagerException.class )
    public void testGetEnvironmentDomain() throws Exception
    {
        environmentManager.getEnvironmentDomain( TestHelper.ENV_ID );

        verify( localPeer ).getVniDomain( TestHelper.VNI );

        doThrow( new PeerException() ).when( localPeer ).getVniDomain( TestHelper.VNI );

        environmentManager.getEnvironmentDomain( TestHelper.ENV_ID );
    }


    @Test( expected = EnvironmentManagerException.class )
    public void testIsContainerInEnvironmentDomain() throws Exception
    {

        doReturn( PORT ).when( environmentContainer ).getDomainPort();

        environmentManager.isContainerInEnvironmentDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID );

        verify( localPeer ).isIpInVniDomain( Common.LOCAL_HOST_IP + ":" + PORT, TestHelper.VNI );

        doThrow( new ContainerHostNotFoundException( "" ) ).when( environment )
                                                           .getContainerHostById( TestHelper.CONTAINER_ID );

        environmentManager.isContainerInEnvironmentDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID );
    }


    @Test
    public void testAddContainerToEnvironmentDomain() throws Exception
    {
        environmentManager.addContainerToEnvironmentDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID, PORT );

        verify( environmentManager ).toggleContainerDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID, PORT, true );
    }


    @Test
    public void testRemoveContainerFromEnvironmentDomain() throws Exception
    {
        environmentManager.removeContainerFromEnvironmentDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID );

        verify( environmentManager ).toggleContainerDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID, -1, false );
    }


    @Test
    public void testToggleContainerDomain() throws Exception
    {
        environmentManager.toggleContainerDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID, PORT, true );

        verify( localPeer ).addIpToVniDomain( Common.LOCAL_HOST_IP + ":" + PORT, TestHelper.VNI );

        doReturn( PORT ).when( environmentContainer ).getDomainPort();

        environmentManager.toggleContainerDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID, -1, false );

        verify( localPeer ).removeIpFromVniDomain( Common.LOCAL_HOST_IP + ":" + PORT, TestHelper.VNI );

        doThrow( new PeerException() ).when( localPeer )
                                      .removeIpFromVniDomain( Common.LOCAL_HOST_IP + ":" + PORT, TestHelper.VNI );

        try
        {
            environmentManager.toggleContainerDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID, -1, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }

        doReturn( EnvironmentStatus.UNDER_MODIFICATION ).when( environment ).getStatus();

        try
        {
            environmentManager.toggleContainerDomain( TestHelper.CONTAINER_ID, TestHelper.ENV_ID, -1, false );

            fail( "Expected EnvironmentModificationException" );
        }
        catch ( EnvironmentModificationException e )
        {
        }
    }


    @Test( expected = EnvironmentModificationException.class )
    public void testSetupSshTunnelForContainer() throws Exception
    {
        environmentManager.setupSshTunnelForContainer( TestHelper.CONTAINER_ID, TestHelper.ENV_ID );

        verify( localPeer ).setupSshTunnelForContainer( Common.LOCAL_HOST_IP, Common.CONTAINER_SSH_TIMEOUT_SEC );

        //-----

        doThrow( new PeerException() ).when( localPeer ).setupSshTunnelForContainer( Common.LOCAL_HOST_IP,
                Common.CONTAINER_SSH_TIMEOUT_SEC );

        environmentManager.setupSshTunnelForContainer( TestHelper.CONTAINER_ID, TestHelper.ENV_ID );
    }


    @Test
    public void testCreateEnvironmentKeyPair() throws Exception
    {
        KeyPair keyPair = mock( KeyPair.class );
        doReturn( keyPair ).when( keyManager ).generateKeyPair( TestHelper.ENV_ID, false );

        PGPSecretKeyRing secRing = mock( PGPSecretKeyRing.class );
        PGPPublicKeyRing pubRing = mock( PGPPublicKeyRing.class );
        doReturn( secRing ).when( pgpKeyUtil ).getSecretKeyRing( any( byte[].class ) );
        doReturn( pubRing ).when( pgpKeyUtil ).getPublicKeyRing( any( byte[].class ) );

        environmentManager.createEnvironmentKeyPair( TestHelper.ENVIRONMENT_ID );

        verify( keyManager ).saveSecretKeyRing( TestHelper.ENV_ID, SecurityKeyType.ENVIRONMENT_KEY.getId(), secRing );
        verify( keyManager ).savePublicKeyRing( TestHelper.ENV_ID, SecurityKeyType.ENVIRONMENT_KEY.getId(), pubRing );
    }


    @Test
    public void testRegisterListener() throws Exception
    {
        EnvironmentEventListener listener = mock( EnvironmentEventListener.class );

        environmentManager.registerListener( listener );

        assertTrue( environmentManager.listeners.contains( listener ) );
    }


    @Test
    public void testUnregisterListener() throws Exception
    {

        EnvironmentEventListener listener = mock( EnvironmentEventListener.class );
        environmentManager.listeners.add( listener );

        environmentManager.unregisterListener( listener );

        assertFalse( environmentManager.listeners.contains( listener ) );
    }


    @Test
    public void testNotifyOnEnvironmentCreated() throws Exception
    {
        EnvironmentEventListener listener = mock( EnvironmentEventListener.class );
        environmentManager.listeners.add( listener );

        environmentManager.notifyOnEnvironmentCreated( environment );

        verify( cachedExecutor ).submit( isA( Runnable.class ) );
    }


    @Test
    public void testNotifyOnEnvironmentGrown() throws Exception
    {
        EnvironmentEventListener listener = mock( EnvironmentEventListener.class );
        environmentManager.listeners.add( listener );

        environmentManager.notifyOnEnvironmentGrown( environment,
                Sets.<EnvironmentContainerHost>newHashSet( environmentContainer ) );

        verify( cachedExecutor ).submit( isA( Runnable.class ) );
    }


    @Test
    public void testNotifyOnContainerDestroyed() throws Exception
    {
        EnvironmentEventListener listener = mock( EnvironmentEventListener.class );
        environmentManager.listeners.add( listener );

        environmentManager.notifyOnContainerDestroyed( environment, TestHelper.CONTAINER_ID );

        verify( cachedExecutor ).submit( isA( Runnable.class ) );
    }


    @Test
    public void testNotifyOnEnvironmentDestroyed() throws Exception
    {
        EnvironmentEventListener listener = mock( EnvironmentEventListener.class );
        environmentManager.listeners.add( listener );

        environmentManager.notifyOnEnvironmentDestroyed( TestHelper.ENV_ID );

        verify( cachedExecutor ).submit( isA( Runnable.class ) );
    }


    @Test
    public void testGetUserId() throws Exception
    {
        environmentManager.getUserId();

        verify( user ).getId();
    }


    @Test
    public void testResolvePeer() throws Exception
    {
        environmentManager.resolvePeer( TestHelper.PEER_ID );

        verify( peerManager ).getPeer( TestHelper.PEER_ID );
    }


    @Test
    public void testSave() throws Exception
    {
        environmentManager.save( environment );

        verify( environmentService ).persist( environment );
    }


    @Test
    public void testUpdate() throws Exception
    {
        doReturn( environment ).when( environmentService ).merge( environment );

        environmentManager.update( environment );

        verify( environmentService, atLeastOnce() ).merge( environment );

        //-----

        BazaarEnvironment bzrEnvironment = mock( BazaarEnvironment.class );
        reset( environmentService );

        environmentManager.update( bzrEnvironment );

        verify( environmentService, never() ).merge( environment );
    }


    @Test
    public void testRemove() throws Exception
    {
        doReturn( true ).when( environmentAdapter ).removeEnvironment( environment );
        environmentManager.remove( environment );

        verify( environmentService ).remove( TestHelper.ENV_ID );
    }


    @Test
    public void testUpdateContainer() throws Exception
    {

        doReturn( environmentContainer ).when( environmentService ).mergeContainer( environmentContainer );

        environmentManager.update( environmentContainer );

        verify( environmentService ).mergeContainer( environmentContainer );
    }


    @Test
    public void testAddAlertHandler() throws Exception
    {
        AlertHandler alertHandler = mock( AlertHandler.class );

        environmentManager.addAlertHandler( alertHandler );

        assertFalse( environmentManager.alertHandlers.containsValue( alertHandler ) );

        //-----

        doReturn( "ID" ).when( alertHandler ).getId();

        environmentManager.addAlertHandler( alertHandler );

        assertTrue( environmentManager.alertHandlers.containsValue( alertHandler ) );
    }


    @Test
    public void testRemoveAlertHandler() throws Exception
    {
        AlertHandler alertHandler = mock( AlertHandler.class );
        environmentManager.alertHandlers.put( "ID", alertHandler );
        doReturn( "ID" ).when( alertHandler ).getId();

        environmentManager.removeAlertHandler( alertHandler );

        assertFalse( environmentManager.alertHandlers.containsValue( alertHandler ) );
    }


    @Test
    public void testGetRegisteredAlertHandlers() throws Exception
    {
        AlertHandler alertHandler = mock( AlertHandler.class );
        environmentManager.alertHandlers.put( "ID", alertHandler );
        doReturn( "ID" ).when( alertHandler ).getId();

        assertFalse( environmentManager.getRegisteredAlertHandlers().isEmpty() );
    }


    @Test
    public void testGetId() throws Exception
    {
        assertNotNull( environmentManager.getId() );
    }


    @Test
    public void testOnAlert() throws Exception
    {
        AlertEvent alertEvent = mock( AlertEvent.class );
        doReturn( TestHelper.ENV_ID ).when( alertEvent ).getEnvironmentId();

        environmentManager.onAlert( alertEvent );

        verify( environmentManager ).handleAlertPack( eq( alertEvent ), isA( EnvironmentAlertHandlers.class ) );
    }


    @Test
    public void testGetEnvironmentAlertHandlers() throws Exception
    {
        EnvironmentAlertHandler environmentAlertHandler = mock( EnvironmentAlertHandler.class );
        doReturn( Sets.newHashSet( environmentAlertHandler ) ).when( environment ).getAlertHandlers();
        doReturn( "ID" ).when( environmentAlertHandler ).getAlertHandlerId();
        doReturn( AlertHandlerPriority.HIGH ).when( environmentAlertHandler ).getAlertHandlerPriority();
        AlertHandler alertHandler = mock( AlertHandler.class );
        environmentManager.alertHandlers.put( "ID", alertHandler );


        assertFalse( environmentManager.getEnvironmentAlertHandlers( TestHelper.ENVIRONMENT_ID ).getAllHandlers()
                                       .isEmpty() );
    }


    @Test
    public void testHandleAlertPack() throws Exception
    {
        EnvironmentAlertHandlers environmentAlertHandlers = mock( EnvironmentAlertHandlers.class );
        EnvironmentAlertHandler environmentAlertHandler = mock( EnvironmentAlertHandler.class );
        AlertEvent alertEvent = mock( AlertEvent.class );
        doReturn( TestHelper.ENV_ID ).when( alertEvent ).getEnvironmentId();
        Map<EnvironmentAlertHandler, AlertEvent> handlersMap = Maps.newHashMap();
        handlersMap.put( environmentAlertHandler, alertEvent );
        doReturn( handlersMap ).when( environmentAlertHandlers ).getAllHandlers();
        AlertHandler alertHandler = mock( AlertHandler.class );
        doReturn( AlertValue.class ).when( alertHandler ).getSupportedAlertValue();
        doReturn( alertHandler ).when( environmentAlertHandlers ).getHandler( environmentAlertHandler );
        AlertValue alertValue = mock( AlertValue.class );
        Alert alert = mock( Alert.class );
        doReturn( alert ).when( alertEvent ).getResource();
        doReturn( alertValue ).when( alert ).getAlertValue( AlertValue.class );

        environmentManager.handleAlertPack( alertEvent, environmentAlertHandlers );

        verify( alertHandler ).postProcess( environment, alertValue );
    }


    @Test
    public void testStartMonitoring() throws Exception
    {
        AlertHandler alertHandler = mock( AlertHandler.class );
        environmentManager.alertHandlers.put( "ID", alertHandler );
        doReturn( environment ).when( environmentManager ).update( environment );

        environmentManager.startMonitoring( "ID", AlertHandlerPriority.HIGH, TestHelper.ENV_ID );

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testStopMonitoring() throws Exception
    {
        doReturn( environment ).when( environmentManager ).update( environment );

        environmentManager.stopMonitoring( "ID", AlertHandlerPriority.HIGH, TestHelper.ENV_ID );

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testOnRegistrationSucceeded() throws Exception
    {
        environmentManager.onRegistrationSucceeded();

        verify( environmentManager ).uploadPeerOwnerEnvironmentsToBazaar();
    }


    @Test
    public void testResetP2pKey() throws Exception
    {
        doNothing().when( environmentManager ).resetP2PSecretKey( anyString(), anyString(), anyLong(), anyBoolean() );
        doReturn( EnvironmentStatus.HEALTHY ).when( environment ).getStatus();

        environmentManager.doResetP2Pkeys();

        verify( environmentManager ).resetP2PSecretKey( anyString(), anyString(), anyLong(), anyBoolean() );
    }


    @Test
    public void testAddSshKeyToEnvironmentEntity() throws Exception
    {
        doReturn( environment ).when( environmentManager ).update( environment );

        environmentManager.addSshKeyToEnvironmentEntity( TestHelper.ENV_ID, TestHelper.SSH_KEY );

        verify( environment ).addSshKey( TestHelper.SSH_KEY );
    }
}
