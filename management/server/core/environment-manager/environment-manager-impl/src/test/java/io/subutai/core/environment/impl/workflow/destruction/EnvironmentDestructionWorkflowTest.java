package io.subutai.core.environment.impl.workflow.destruction;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentDestructionWorkflowTest
{
    EnvironmentDestructionWorkflow workflow;

    @Mock
    EnvironmentManagerImpl environmentManager;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();


    class EnvironmentDestructionWorkflowSUT extends EnvironmentDestructionWorkflow
    {
        public EnvironmentDestructionWorkflowSUT( final EnvironmentManagerImpl environmentManager,
                                                  final LocalEnvironment environment,
                                                  final TrackerOperation operationTracker )
        {
            super( environmentManager, environment, operationTracker );
        }


        public void addStep( EnvironmentDestructionWorkflow.EnvironmentDestructionPhase stepname )
        {
            //no-op
        }
    }


    @Before
    public void setUp() throws Exception
    {
        workflow = spy( new EnvironmentDestructionWorkflowSUT( environmentManager, environment, trackerOperation ) );
        doReturn( environment ).when( environmentManager ).update( environment );
    }


    @Test
    public void testINIT() throws Exception
    {
        workflow.INIT();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testCLEANUP_ENVIRONMENT() throws Exception
    {
        workflow.CLEANUP_ENVIRONMENT();

        verify( environmentManager, atLeastOnce() ).update( environment );
    }


    @Test
    public void testFINALIZE() throws Exception
    {
        RelationManager relationManager = mock( RelationManager.class );
        doReturn( relationManager ).when( environmentManager ).getRelationManager();
        doReturn( Sets.newHashSet( TestHelper.ENV_CONTAINER() ) ).when( environment ).getContainerHosts();

        workflow.FINALIZE();

        verify( environmentManager, atLeastOnce() ).remove( environment );
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
