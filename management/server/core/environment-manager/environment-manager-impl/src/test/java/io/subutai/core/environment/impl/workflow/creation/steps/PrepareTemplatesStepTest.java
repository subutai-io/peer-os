package io.subutai.core.environment.impl.workflow.creation.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class PrepareTemplatesStepTest
{
    PrepareTemplatesStep step;

    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    @Mock
    PeerManager peerManager;
    @Mock
    Topology topology;
    @Mock
    PeerUtil<PrepareTemplatesResponse> peerUtil;
    @Mock
    PeerUtil.PeerTaskResults<PrepareTemplatesResponse> peerTaskResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;
    Peer peer = TestHelper.PEER();
    @Mock
    PrepareTemplatesResponse response;


    @Before
    public void setUp() throws Exception
    {
        step = new PrepareTemplatesStep( environment, peerManager, topology, TestHelper.KURJUN_TOKEN,
                TestHelper.TRACKER_OPERATION() );
        step.templateUtil = peerUtil;
        doReturn( response ).when( peerTaskResult ).getResult();

        TestHelper.bind( environment, peer, peerUtil, peerTaskResults, peerTaskResult );
    }


    @Test( expected = EnvironmentCreationException.class )
    public void testExecute() throws Exception
    {
        doReturn( true ).when( response ).hasSucceeded();

        step.execute();

        verify( peerUtil ).executeParallelFailFast();

        doReturn( false ).when( response ).hasSucceeded();

        step.execute();
    }
}
