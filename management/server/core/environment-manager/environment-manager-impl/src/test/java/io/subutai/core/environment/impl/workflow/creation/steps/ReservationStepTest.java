package io.subutai.core.environment.impl.workflow.creation.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ReservationStepTest
{

    ReservationStep step;
    @Mock
    Topology topology;
    EnvironmentImpl environment = TestHelper.ENVIRONMENT();
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();
    @Mock
    PeerUtil<Object> peerUtil;
    @Mock
    PeerUtil.PeerTaskResults<Object> peerTaskResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;
    Peer peer = TestHelper.PEER();
    @Mock
    PeerManager peerManager;


    @Before
    public void setUp() throws Exception
    {
        step = new ReservationStep( topology, environment, peerManager, TestHelper.TRACKER_OPERATION() );
        TestHelper.bind( environment, peer, peerUtil, peerTaskResults, peerTaskResult );
        step.peerUtil = peerUtil;
        doReturn( Sets.newHashSet( peer ) ).when( peerManager ).resolve( anySet() );
    }


    @Test( expected = EnvironmentCreationException.class )
    public void testExecute() throws Exception
    {
        doReturn( 123 ).when( peerTaskResult ).getResult();
        doReturn( true ).when( peerTaskResult ).hasSucceeded();

        step.execute();

        verify( environment ).setP2PSubnet( anyString() );

        doReturn( false ).when( peerTaskResult ).hasSucceeded();
        doReturn( true ).when( peerTaskResults ).hasFailures();

        step.execute();
    }
}
