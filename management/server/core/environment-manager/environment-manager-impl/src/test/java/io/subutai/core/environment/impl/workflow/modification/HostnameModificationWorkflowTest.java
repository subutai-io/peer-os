package io.subutai.core.environment.impl.workflow.modification;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.ContainerId;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class HostnameModificationWorkflowTest
{

    class HostnameModificationWorkflowSUT extends HostnameModificationWorkflow
    {
        public HostnameModificationWorkflowSUT( final LocalEnvironment environment, final ContainerId containerId,
                                                final String newHostname, final TrackerOperation operationTracker,
                                                final EnvironmentManagerImpl environmentManager )
        {
            super( environment, containerId, newHostname, operationTracker, environmentManager );
        }


        public void addStep( HostnameModificationWorkflow.HostnameModificationPhase step )
        {
            //no-op
        }
    }


    HostnameModificationWorkflow workflow;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    @Mock
    EnvironmentManagerImpl environmentManager;
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();
    private static final String NEW_HOSTNAME = "new";


    @Before
    public void setUp() throws Exception
    {
        workflow = new HostnameModificationWorkflowSUT( environment, TestHelper.CONT_HOST_ID, NEW_HOSTNAME,
                trackerOperation, environmentManager );
        doReturn( environment ).when( environmentManager ).update( environment );
    }


    @Test
    public void testINIT() throws Exception
    {
        workflow.INIT();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testCHANGE_HOSTNAME() throws Exception
    {
        workflow.CHANGE_HOSTNAME();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testUPDATE_ETC_HOSTS_FILE() throws Exception
    {
        workflow.UPDATE_ETC_HOSTS_FILE();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testUPDATE_AUTHORIZED_KEYS_FILE() throws Exception
    {
        workflow.UPDATE_AUTHORIZED_KEYS_FILE();

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
