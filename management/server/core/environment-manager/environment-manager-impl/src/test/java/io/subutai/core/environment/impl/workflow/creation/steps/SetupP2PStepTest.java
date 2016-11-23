package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class SetupP2PStepTest
{
    SetupP2PStep step;
    @Mock
    Topology topology;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    @Mock
    PeerUtil<Object> peerUtil;
    @Mock
    PeerUtil.PeerTaskResults<Object> peerTaskResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;
    @Mock
    PeerManager peerManager;
    Peer peer = TestHelper.PEER();


    @Before
    public void setUp() throws Exception
    {
        step = new SetupP2PStep( topology, environment, TestHelper.TRACKER_OPERATION() );
        step.peerUtil = peerUtil;
        TestHelper.bind( environment, peer, peerUtil, peerTaskResults, peerTaskResult );
        Map<String, Set<String>> peerRhIds = Maps.newHashMap();
        peerRhIds.put( peer.getId(), Sets.newHashSet(TestHelper.RH_ID) );
        doReturn(peerRhIds).when( topology ).getPeerRhIds();
    }


    @Test
    public void testExecute() throws Exception
    {
        step.execute();

        verify( peerUtil, times( 2 ) ).executeParallel();
    }
}
