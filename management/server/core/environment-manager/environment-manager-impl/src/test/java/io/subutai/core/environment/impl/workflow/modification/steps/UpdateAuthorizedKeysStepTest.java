package io.subutai.core.environment.impl.workflow.modification.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.Peer;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class UpdateAuthorizedKeysStepTest
{

    UpdateAuthorizedKeysStep step;

    EnvironmentImpl environment = TestHelper.ENVIRONMENT();
    private static final String OLD_HOSTNAME = "old";
    private static final String NEW_HOSTNAME = "new";
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();

    @Mock
    PeerUtil<Object> peerUtil;
    @Mock
    PeerUtil.PeerTaskResults<Object> peerTaskResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;
    Peer peer = TestHelper.PEER();


    @Before
    public void setUp() throws Exception
    {
        step = new UpdateAuthorizedKeysStep( environment, OLD_HOSTNAME, NEW_HOSTNAME, trackerOperation );
        step.peerUtil = peerUtil;
        TestHelper.bind( environment, peer, peerUtil, peerTaskResults, peerTaskResult );
    }


    @Test
    public void testExecute() throws Exception
    {
        doReturn( true ).doReturn( false ).when( peerTaskResult ).hasSucceeded();

        step.execute();

        step.execute();

        verify( peerUtil, times( 2 ) ).executeParallel();
    }
}
