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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class RemoveSshKeyStepTest
{
    RemoveSshKeyStep step;

    LocalEnvironment environment = TestHelper.ENVIRONMENT();
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
        step = new RemoveSshKeyStep( TestHelper.SSH_KEY, environment, TestHelper.TRACKER_OPERATION() );
        TestHelper.bind( environment, peer, peerUtil, peerTaskResults, peerTaskResult );
        step.keyUtil = peerUtil;
    }


    @Test( expected = EnvironmentManagerException.class )
    public void testExecute() throws Exception
    {
        step.execute();

        verify( environment ).removeSshKey( TestHelper.SSH_KEY );

        doReturn( false ).when( peerTaskResult ).hasSucceeded();
        doReturn( true ).when( peerTaskResults ).hasFailures();

        step.execute();
    }
}
