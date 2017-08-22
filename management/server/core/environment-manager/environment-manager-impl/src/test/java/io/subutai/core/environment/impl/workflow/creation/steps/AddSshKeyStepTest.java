package io.subutai.core.environment.impl.workflow.creation.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.Peer;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class AddSshKeyStepTest
{

    AddSshKeyStep step;

    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    Peer PEER = TestHelper.PEER();
    @Mock
    PeerUtil<Object> PEER_UTIL;
    @Mock
    PeerUtil.PeerTaskResults<Object> keyResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;


    @Before
    public void setUp() throws Exception
    {
        step = new AddSshKeyStep( TestHelper.SSH_KEY, environment, TestHelper.TRACKER_OPERATION() );
        step.keyUtil = PEER_UTIL;

        TestHelper.bind( environment, PEER, PEER_UTIL, keyResults, peerTaskResult );
    }


    @Test( expected = EnvironmentManagerException.class )
    public void testExecute() throws Exception
    {
        step.execute();

        verify( PEER_UTIL ).addPeerTask( any( PeerUtil.PeerTask.class ) );

        doReturn( true ).when( peerTaskResult ).hasSucceeded();

        step.execute();

        doReturn( true ).when( keyResults ).hasFailures();

        step.execute();
    }
}
