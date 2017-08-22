package io.subutai.core.environment.impl.workflow.modification;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class SshKeyAdditionWorkflowTest
{


    class SshKeyAdditionWorkflowSUT extends SshKeyAdditionWorkflow
    {
        public SshKeyAdditionWorkflowSUT( final LocalEnvironment environment, final String sshKey,
                                          final TrackerOperation operationTracker,
                                          final EnvironmentManagerImpl environmentManager )
        {
            super( environment, sshKey, operationTracker, environmentManager );
        }


        public void addStep( SshKeyAdditionWorkflow.SshKeyAdditionPhase step )
        {
            //no-op
        }
    }


    SshKeyAdditionWorkflow workflow;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    @Mock
    EnvironmentManagerImpl environmentManager;
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();


    @Before
    public void setUp() throws Exception
    {

        workflow =
                new SshKeyAdditionWorkflowSUT( environment, TestHelper.SSH_KEY, trackerOperation, environmentManager );

        doReturn( environment ).when( environmentManager ).update( environment );
    }


    @Test
    public void testINIT() throws Exception
    {
        workflow.INIT();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testADD_KEY() throws Exception
    {
        workflow.ADD_KEY();

        verify( environmentManager, atLeastOnce() ).update( environment );
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
