package io.subutai.core.environment.impl.workflow.destruction.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.peer.Peer;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class CleanupEnvironmentStepTest
{
    CleanupEnvironmentStep step;

    LocalEnvironment environment = TestHelper.ENVIRONMENT();

    @Mock
    PeerUtil<Object> peerUtil;
    @Mock
    PeerUtil.PeerTaskResults<Object> peerTaskResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;
    Peer peer = TestHelper.PEER();
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();


    @Before
    public void setUp() throws Exception
    {
        step = new CleanupEnvironmentStep( environment, trackerOperation);
        step.cleanupUtil = peerUtil;

        TestHelper.bind( environment, peer, peerUtil, peerTaskResults, peerTaskResult );
    }


    @Test
    public void testExecute() throws Exception
    {
        doReturn( true ).when( peerTaskResult ).hasSucceeded();

        step.execute();

        verify( trackerOperation ).addLog( anyString() );

        doReturn( false ).when( peerTaskResult ).hasSucceeded();

        step.execute();

        verify( trackerOperation, times( 2 ) ).addLog( anyString() );

        doReturn( Sets.newHashSet() ).when( environment ).getPeers();
        reset( trackerOperation );

        step.execute();

        verify( trackerOperation, never() ).addLog( anyString() );
    }
}
