package io.subutai.core.environment.impl.workflow.modification;


import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentModifyWorkflowTest
{
    EnvironmentModifyWorkflow workflow;


    class EnvironmentModifyWorkflowSUT extends EnvironmentModifyWorkflow
    {
        public EnvironmentModifyWorkflowSUT( final String defaultDomain, final IdentityManager identityManager,
                                             final PeerManager peerManager, final SecurityManager securityManager,
                                             final LocalEnvironment environment, final Topology topology,
                                             final Set<String> removedContainers,
                                             final Map<String, ContainerQuota> changedContainers,
                                             final TrackerOperation operationTracker,
                                             final EnvironmentManagerImpl environmentManager )
        {
            super( defaultDomain, identityManager, peerManager, securityManager, environment, topology,
                    removedContainers, changedContainers, operationTracker, environmentManager );
        }


        public void addStep( EnvironmentGrowingPhase step )
        {
            //no-op
        }
    }


    @Mock
    EnvironmentManagerImpl environmentManager;
    @Mock
    PeerManager peerManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    SecurityManager securityManager;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    @Mock
    Topology topology;

    @Mock
    PeerUtil<Object> peerUtil;
    @Mock
    PeerUtil.PeerTaskResults<Object> peerTaskResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;

    Peer peer = TestHelper.PEER();
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();


    @Before
    public void setUp() throws Exception
    {
        Map<String, ContainerQuota> changedContainers = Maps.newHashMap();
        changedContainers.put( TestHelper.CONTAINER_ID, new ContainerQuota( ContainerSize.LARGE ) );

        workflow = new EnvironmentModifyWorkflowSUT( Common.DEFAULT_DOMAIN_NAME, identityManager, peerManager,
                securityManager, environment, topology, Sets.newHashSet( TestHelper.CONTAINER_ID ), changedContainers,
                trackerOperation, environmentManager );

        doReturn( environment ).when( environmentManager ).update( environment );
        doReturn( environmentContainer ).when( environment ).getContainerHostById( TestHelper.CONTAINER_ID );
    }


    @Test
    public void testINIT() throws Exception
    {
        workflow.INIT();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testMODIFY_QUOTA() throws Exception
    {
        workflow.MODIFY_QUOTA();

        verify( environmentManager ).loadEnvironment( TestHelper.ENV_ID );
    }


    @Test
    public void testDESTROY_CONTAINERS() throws Exception
    {
        workflow.DESTROY_CONTAINERS();

        verify( trackerOperation, atLeastOnce() ).addLog( anyString() );
    }


    @Test
    public void testGENERATE_KEYS() throws Exception
    {
        workflow.GENERATE_KEYS();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testRESERVE_NET() throws Exception
    {
        workflow.RESERVE_NET();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testSETUP_P2P() throws Exception
    {
        workflow.SETUP_P2P();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testPREPARE_TEMPLATES() throws Exception
    {
        workflow.PREPARE_TEMPLATES();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testCLONE_CONTAINERS() throws Exception
    {
        workflow.CLONE_CONTAINERS();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testCONFIGURE_HOSTS() throws Exception
    {
        workflow.CONFIGURE_HOSTS();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testCONFIGURE_SSH() throws Exception
    {
        workflow.CONFIGURE_SSH();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testSET_QUOTA() throws Exception
    {
        workflow.SET_QUOTA();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testFINALIZE() throws Exception
    {
        workflow.FINALIZE();

        verify( trackerOperation ).addLogDone( anyString() );
    }


    @Test
    public void testFail() throws Exception
    {
        Throwable throwable = mock( Throwable.class );
        workflow.fail( "", throwable );

        verify( trackerOperation ).addLogFailed( anyString() );
    }


    @Test
    public void testOnCancellation() throws Exception
    {
        workflow.onCancellation();

        verify( trackerOperation ).addLogFailed( anyString() );
    }
}
