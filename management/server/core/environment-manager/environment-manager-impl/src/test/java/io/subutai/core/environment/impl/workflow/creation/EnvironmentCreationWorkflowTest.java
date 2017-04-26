package io.subutai.core.environment.impl.workflow.creation;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.environment.Topology;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentCreationWorkflowTest
{
    EnvironmentCreationWorkflow workflow;

    @Mock
    EnvironmentManagerImpl environmentManager;
    @Mock
    PeerManager peerManager;
    @Mock
    SecurityManager securityManager;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    @Mock
    Topology topology;
    @Mock
    IdentityManager identityManager;


    class EnvironmentCreationWorkflowSUT extends EnvironmentCreationWorkflow
    {


        public EnvironmentCreationWorkflowSUT( final String defaultDomain, final IdentityManager identityManager,
                                               final EnvironmentManagerImpl environmentManager,
                                               final PeerManager peerManager, final SecurityManager securityManager,
                                               final LocalEnvironment environment, final Topology topology,
                                               final String sshKey, final TrackerOperation operationTracker )
        {
            super( defaultDomain, identityManager, environmentManager, peerManager, securityManager, environment,
                    topology, sshKey, operationTracker );
        }


        public void addStep( EnvironmentCreationWorkflow.EnvironmentCreationPhase stepname )
        {
            //no-op
        }
    }


    @Before
    public void setUp() throws Exception
    {
        workflow = new EnvironmentCreationWorkflowSUT( Common.DEFAULT_DOMAIN_NAME, identityManager, environmentManager,
                peerManager, securityManager, environment, topology, TestHelper.SSH_KEY,
                TestHelper.TRACKER_OPERATION() );
        doReturn( environment ).when( environmentManager ).update( environment );
    }


    @Test
    public void testINIT() throws Exception
    {
        workflow.INIT();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testGENERATE_KEYS() throws Exception
    {
        workflow.GENERATE_KEYS();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testRESERVE_NET() throws Exception
    {
        workflow.RESERVE_NET();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testSETUP_P2P() throws Exception
    {
        workflow.SETUP_P2P();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testPREPARE_TEMPLATES() throws Exception
    {
        workflow.PREPARE_TEMPLATES();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testCLONE_CONTAINERS() throws Exception
    {
        workflow.CLONE_CONTAINERS();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testCONFIGURE_HOSTS() throws Exception
    {
        workflow.CONFIGURE_HOSTS();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testCONFIGURE_SSH() throws Exception
    {

        workflow.CONFIGURE_SSH();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testSET_QUOTA() throws Exception
    {
        workflow.SET_QUOTA();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testFINALIZE() throws Exception
    {
        workflow.FINALIZE();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testOnCancellation() throws Exception
    {
        workflow.onCancellation();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }
}
