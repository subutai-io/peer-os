package io.subutai.core.environment.impl;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.security.auth.Subject;

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
import io.subutai.common.environment.EnvironmentPeer;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.core.environment.impl.adapter.ProxyEnvironment;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
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
import io.subutai.core.tracker.api.Tracker;
import io.subutai.hub.share.common.HubAdapter;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerImplTest
{

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
    HubAdapter hubAdapter;
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

    EnvironmentImpl environment = TestHelper.ENVIRONMENT();

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


    class EnvironmentManagerImplSUT extends EnvironmentManagerImpl
    {
        public EnvironmentManagerImplSUT( final PeerManager peerManager, final SecurityManager securityManager,
                                          final IdentityManager identityManager, final Tracker tracker,
                                          final RelationManager relationManager, final HubAdapter hubAdapter,
                                          final EnvironmentService environmentService )
        {
            super( peerManager, securityManager, identityManager, tracker, relationManager, hubAdapter,
                    environmentService );
        }


        protected EnvironmentAdapter getEnvironmentAdapter( HubAdapter hubAdapter )
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

        environmentManager = spy( new EnvironmentManagerImplSUT( peerManager, securityManager, identityManager, tracker,
                relationManager, hubAdapter, environmentService ) );
        environmentManager.jsonUtil = jsonUtil;

        doReturn( Sets.newHashSet( environment ) ).when( environmentService ).getAll();
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
        doReturn( trackerOperation ).when( tracker )
                                    .createTrackerOperation( eq( environmentManager.MODULE_NAME ), anyString() );
        doReturn( user ).when( identityManager ).getActiveUser();
        doReturn( TestHelper.USER_ID ).when( user ).getId();
        doReturn( userDelegate ).when( identityManager ).getUserDelegate( TestHelper.USER_ID );
        doReturn( TestHelper.USER_ID.toString() ).when( userDelegate ).getId();
        doReturn( environment ).when( environmentManager ).loadEnvironment( TestHelper.ENV_ID );
    }


    @Test
    public void testDispose() throws Exception
    {
        environmentManager.registerActiveWorkflow( environment, checkWorkflow );

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
        doReturn( Sets.newHashSet(environmentContainer) ).when( environment ).getContainerHosts();
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
        doReturn( secretKeyRing ).when( environmentManager ).createEnvironmentKeyPair( any( EnvironmentId.class ),
                eq( TestHelper.USER_ID.toString() ) );

        environmentManager.createEmptyEnvironment( topology );

        verify( environmentManager ).save( any( EnvironmentImpl.class ) );
    }


    @Test
    public void testGrowEnvironment() throws Exception
    {

        doReturn( environmentCreationRef ).when( environmentManager )
                                          .modifyEnvironment( eq( TestHelper.ENV_ID ), eq( topology ), anyList(),
                                                  anyMap(), anyBoolean() );

        environmentManager.growEnvironment( TestHelper.ENV_ID, topology, false );

        verify( environmentManager )
                .modifyEnvironment( eq( TestHelper.ENV_ID ), eq( topology ), anyList(), anyMap(), anyBoolean() );
    }


    @Test
    public void testModifyEnvironment() throws Exception
    {
        List<String> removedContainers = Lists.newArrayList( TestHelper.CONTAINER_ID );
        Map<String, ContainerSize> changedContainers = Maps.newHashMap();
        changedContainers.put( TestHelper.CONTAINER_ID, ContainerSize.LARGE );
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

        ProxyEnvironment proxyEnvironment = mock( ProxyEnvironment.class );

        doReturn( proxyEnvironment ).when( environmentManager ).loadEnvironment( TestHelper.ENV_ID );

        environmentManager.destroyEnvironment( TestHelper.ENV_ID, false );

        verify( environmentAdapter ).removeEnvironment( proxyEnvironment );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {
        ContainerDestructionWorkflow containerDestructionWorkflow = mock( ContainerDestructionWorkflow.class );
        doReturn( containerDestructionWorkflow ).when( environmentManager )
                                                .getContainerDestructionWorkflow( environment, environmentContainer,
                                                        trackerOperation );
        doNothing().when( environmentManager ).registerActiveWorkflow( environment, containerDestructionWorkflow );
        doReturn( environmentContainer ).when( environment ).getContainerHostById( TestHelper.CONTAINER_ID );

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

        verify( trackerOperation ).addLogFailed( anyString() );

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

        verify( trackerOperation, times( 2 ) ).addLogFailed( anyString() );

        //-----

        ProxyEnvironment proxyEnvironment = mock( ProxyEnvironment.class );

        doReturn( proxyEnvironment ).when( environmentManager ).loadEnvironment( TestHelper.ENV_ID );

        environmentManager.destroyContainer( TestHelper.ENV_ID, TestHelper.CONTAINER_ID, false );

        verify( environmentAdapter ).destroyContainer( proxyEnvironment, TestHelper.CONTAINER_ID );
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
}
