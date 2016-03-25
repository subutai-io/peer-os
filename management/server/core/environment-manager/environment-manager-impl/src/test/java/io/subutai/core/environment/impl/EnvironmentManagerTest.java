package io.subutai.core.environment.impl;


import java.util.HashSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.dao.EnvironmentDataService;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.EnvironmentCreationWorkflow;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.strategy.api.StrategyManager;
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
    DaoManager daoManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    RelationManager relationManager;
    @Mock
    Tracker tracker;
    @Mock
    Peer peer;
    @Mock
    Node node;
    @Mock
    User user;
    @Mock
    EnvironmentDataService environmentDataService;
    @Mock
    EnvironmentImpl environment;
    @Mock
    EnvironmentCreationWorkflow environmentCreationWorkflow;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    Topology topology;

    @Mock
    private StrategyManager strategyManager;
    @Mock
    private QuotaManager quotaManager;


    @Before
    public void setUp() throws Exception
    {
        when( node.getPeerId() ).thenReturn( PEER_ID );
        when( peerManager.getPeer( PEER_ID ) ).thenReturn( peer );
        doReturn( true ).when( peer ).isOnline();

        //        blueprint = new Blueprint( "env", null, Sets.newHashSet( nodeGroup ) );

        environmentManager =
                spy( new EnvironmentManagerImpl( templateRegistry, peerManager, securityManager, daoManager,
                        identityManager, tracker, relationManager ) );
        doReturn( environment ).when( environmentManager )
                               .createEmptyEnvironment( anyString(), anyString(), anyString() );
        //        doReturn( topology ).when( environmentManager ).buildTopology( blueprint );
        doReturn( new HashSet<>() ).when( environmentManager ).getUsedIps( ( Peer ) any() );
        doReturn( environmentCreationWorkflow ).when( environmentManager )
                                               .getEnvironmentCreationWorkflow( any( EnvironmentImpl.class ),
                                                       any( Topology.class ), anyString(),
                                                       any( TrackerOperation.class ) );
        environmentManager.environmentDataService = environmentDataService;
        //doReturn( user ).when( identityManager ).getUser();
        doReturn( environment ).when( environmentDataService ).find( anyString() );
        doReturn( ENV_ID ).when( environment ).getId();
        doReturn( trackerOperation ).when( tracker ).createTrackerOperation( anyString(), anyString() );
    }


    @Test
    @Ignore
    public void testCreateEnvironment() throws Exception
    {
        Environment environment1 = environmentManager.createEnvironment( topology, true );

        assertEquals( environment1, environment );
    }
}
