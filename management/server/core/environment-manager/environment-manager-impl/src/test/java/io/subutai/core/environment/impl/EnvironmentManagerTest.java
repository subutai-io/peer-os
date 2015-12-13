package io.subutai.core.environment.impl;


import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.dao.EnvironmentDataService;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.relation.TrustRelationManager;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.tracker.api.Tracker;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerTest
{
    private static final String ENV_ID = "123";
    private static final java.lang.String PEER_ID = "peer_id";
    EnvironmentManagerImpl environmentManager;


    @Mock
    TemplateManager templateRegistry;
    @Mock
    PeerManager peerManager;
    @Mock
    SecurityManager securityManager;
    @Mock
    NetworkManager networkManager;
    @Mock
    DaoManager daoManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    TrustRelationManager relationManager;
    @Mock
    Tracker tracker;
    @Mock
    Peer peer;
    @Mock
    NodeGroup nodeGroup;
    @Mock
    User user;
    @Mock
    EnvironmentDataService environmentDataService;
    @Mock
    EnvironmentImpl environment;
    @Mock
    EnvironmentCreationWorkflow environmentCreationWorkflow;

    @Mock
    Topology topology;

    Blueprint blueprint;


    @Before
    public void setUp() throws Exception
    {
        when( nodeGroup.getPeerId() ).thenReturn( PEER_ID );
        when( peerManager.getPeer( PEER_ID ) ).thenReturn( peer );

        blueprint = new Blueprint( "env", null, Sets.newHashSet( nodeGroup ) );

        environmentManager =
                spy( new EnvironmentManagerImpl( templateRegistry, peerManager, securityManager, networkManager,
                        daoManager, identityManager, tracker, relationManager ) );
        doReturn( environment ).when( environmentManager )
                               .createEmptyEnvironment( anyString(), anyString(), anyString(), any( Blueprint.class ) );
        //        doReturn( topology ).when( environmentManager ).buildTopology( blueprint );
        doReturn( new HashSet<>() ).when( environmentManager ).getUsedGateways( ( Peer ) any() );
        doReturn( environmentCreationWorkflow ).when( environmentManager )
                                               .getEnvironmentCreationWorkflow( any( EnvironmentImpl.class ),
                                                       any( Topology.class ), anyString(),
                                                       any( TrackerOperation.class ) );
        environmentManager.environmentDataService = environmentDataService;
        //doReturn( user ).when( identityManager ).getUser();
        doReturn( environment ).when( environmentDataService ).find( anyString() );
        doReturn( ENV_ID ).when( environment ).getId();
    }


    @Test
    public void testCreateEnvironment() throws Exception
    {
        Environment environment1 = environmentManager.createEnvironment( blueprint, true );

        assertEquals( environment1, environment );
    }
}
