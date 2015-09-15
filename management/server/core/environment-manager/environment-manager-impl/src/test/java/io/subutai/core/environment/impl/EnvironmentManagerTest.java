package io.subutai.core.environment.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.dao.EnvironmentDataService;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.EnvironmentCreationWorkflow;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.User;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.tracker.api.Tracker;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerTest
{
    private static final String ENV_ID = "123";
    EnvironmentManagerImpl environmentManager;


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
    io.subutai.core.security.api.SecurityManager securityManager;
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

    Topology topology = new Topology();


    @Before
    public void setUp() throws Exception
    {
        topology.addNodeGroupPlacement( peer, nodeGroup );
        environmentManager = spy( new EnvironmentManagerImpl( templateRegistry, peerManager, networkManager, daoManager,
                Common.DEFAULT_DOMAIN_NAME, identityManager, tracker, securityManager ) );
        doReturn( environment ).when( environmentManager )
                               .createEmptyEnvironment( anyString(), anyString(), anyString() );
        doReturn( environmentCreationWorkflow ).when( environmentManager )
                                               .getEnvironmentCreationWorkflow( any( Environment.class ),
                                                       any( Topology.class ), anyString(), anyString(),
                                                       any( TrackerOperation.class ) );
        environmentManager.environmentDataService = environmentDataService;
        doReturn( user ).when( identityManager ).getUser();
        doReturn( environment ).when( environmentDataService ).find( anyString() );
        doReturn( ENV_ID ).when( environment ).getId();
    }


    @Test
    public void testCreateEnvironment() throws Exception
    {
        Environment environment1 =
                environmentManager.createEnvironment( "env", topology, "192.168.1.0/24", null, true );

        assertEquals( environment1, environment );
    }
}
