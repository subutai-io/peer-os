package io.subutai.core.environment.impl;


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

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentCreationRef;
import io.subutai.common.environment.EnvironmentPeer;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
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

        verify( environmentManager ).modifyEnvironment(  eq( TestHelper.ENV_ID ), eq( topology ), anyList(),
                anyMap(), anyBoolean() );
    }
}
