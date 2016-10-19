package io.subutai.core.environment.impl.workflow.modification;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class P2PSecretKeyModificationWorkflowTest
{

    class P2PSecretKeyModificationWorkflowSUT extends P2PSecretKeyModificationWorkflow
    {
        public P2PSecretKeyModificationWorkflowSUT( final EnvironmentImpl environment, final String p2pSecretKey,
                                                    final long p2pSecretKeyTtlSeconds,
                                                    final TrackerOperation operationTracker,
                                                    final EnvironmentManagerImpl environmentManager )
        {
            super( environment, p2pSecretKey, p2pSecretKeyTtlSeconds, operationTracker, environmentManager );
        }


        public void addStep( P2PSecretKeyModificationWorkflow.P2PSecretKeyModificationPhase step )
        {
            //no-op
        }
    }


    P2PSecretKeyModificationWorkflow workflow;
    EnvironmentImpl environment = TestHelper.ENVIRONMENT();
    @Mock
    EnvironmentManagerImpl environmentManager;
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();

    private static final String SECRET = "secret";
    private static final Long TTL = 123L;


    @Before
    public void setUp() throws Exception
    {
        workflow = new P2PSecretKeyModificationWorkflowSUT( environment, SECRET, TTL, trackerOperation,
                environmentManager );
        doReturn( environment ).when( environmentManager ).update( environment );
    }


    @Test
    public void testINIT() throws Exception
    {
        workflow.INIT();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testREPLACE_KEY() throws Exception
    {
        workflow.REPLACE_KEY();

        verify( environmentManager ).update( environment );
    }


    @Test
    public void testFINALIZE() throws Exception
    {
        workflow.FINALIZE();

        verify( trackerOperation ).addLogDone( anyString() );
    }

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
