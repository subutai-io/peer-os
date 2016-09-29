package io.subutai.core.environment.impl.workflow.modification.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class P2PSecretKeyResetStepTest
{
    P2PSecretKeyResetStep step;

    EnvironmentImpl environment = TestHelper.ENVIRONMENT();
    @Mock
    P2PCredentials p2PCredentials;
    @Mock
    TrackerOperation trackerOperation;
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
        step = new P2PSecretKeyResetStep( environment, p2PCredentials, trackerOperation );
        step.resetUtil = peerUtil;
        TestHelper.bind( environment, peer, peerUtil, peerTaskResults, peerTaskResult );
    }


    @Test( expected = PeerException.class )
    public void testExecute() throws Exception
    {
        doReturn( true ).when( peerTaskResult ).hasSucceeded();

        step.execute();

        verify( peerUtil ).executeParallel();

        doReturn( false ).when( peerTaskResult ).hasSucceeded();
        doReturn( true ).when( peerTaskResults ).hasFailures();

        step.execute();
    }
}
