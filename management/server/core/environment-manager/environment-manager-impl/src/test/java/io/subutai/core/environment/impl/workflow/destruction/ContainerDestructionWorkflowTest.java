package io.subutai.core.environment.impl.workflow.destruction;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ContainerDestructionWorkflowTest
{
    ContainerDestructionWorkflow workflow;

    @Mock
    EnvironmentManagerImpl environmentManager;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    EnvironmentContainerImpl container = TestHelper.ENV_CONTAINER();
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();


    class ContainerDestructionWorkflowSUT extends ContainerDestructionWorkflow
    {


        public ContainerDestructionWorkflowSUT( final EnvironmentManagerImpl environmentManager,
                                                final LocalEnvironment environment, final ContainerHost containerHost,
                                                final TrackerOperation operationTracker )
        {
            super( environmentManager, environment, containerHost, operationTracker );
        }


        public void addStep( ContainerDestructionWorkflow.ContainerDestructionPhase stepname )
        {
            //no-op
        }
    }


    @Before
    public void setUp() throws Exception
    {
        workflow = new ContainerDestructionWorkflowSUT( environmentManager, environment, container, trackerOperation );
        doReturn( environment ).when( environmentManager ).update( environment );
    }


    @Test
    public void testINIT() throws Exception
    {
        workflow.INIT();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testVALIDATE() throws Exception
    {
        assertNull( workflow.VALIDATE() );

        doReturn( Sets.newHashSet( container, TestHelper.ENV_CONTAINER() ) ).when( environment ).getContainerHosts();

        assertEquals( ContainerDestructionWorkflow.ContainerDestructionPhase.DESTROY_CONTAINER, workflow.VALIDATE() );
    }


    @Test
    public void testDESTROY_CONTAINER() throws Exception
    {

        RelationManager relationManager = mock( RelationManager.class );
        doReturn( relationManager ).when( environmentManager ).getRelationManager();

        workflow.DESTROY_CONTAINER();

        verify( environmentManager, atLeastOnce() ).update( any( LocalEnvironment.class ) );
    }


    @Test
    public void testFINALIZE() throws Exception
    {
        workflow.FINALIZE();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testFail() throws Exception
    {
        workflow.fail( "", null );

        verify( trackerOperation, atLeastOnce() ).addLogFailed( anyString() );
    }


    @Test
    public void testOnCancellation() throws Exception
    {
        workflow.onCancellation();

        verify( trackerOperation, atLeastOnce() ).addLogFailed( anyString() );
    }
}
