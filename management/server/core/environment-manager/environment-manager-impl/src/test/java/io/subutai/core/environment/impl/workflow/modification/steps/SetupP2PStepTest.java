package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.P2pIps;
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
    Peer peer = TestHelper.PEER();
    @Mock
    PeerManager peerManager;
    @Mock
    P2pIps p2pIps;
    @Mock
    RhP2pIp rhP2pIp;


    @Before
    public void setUp() throws Exception
    {
        step = new SetupP2PStep( topology, environment, TestHelper.TRACKER_OPERATION() );
        step.peerUtil = peerUtil;
        TestHelper.bind( environment, peer, peerUtil, peerTaskResults, peerTaskResult );
        Map<String, Set<String>> peerRhIds = Maps.newHashMap();
        peerRhIds.put( peer.getId(), Sets.newHashSet(TestHelper.RH_ID) );
        doReturn(peerRhIds).when( topology ).getPeerRhIds();
        doReturn( p2pIps ).when( environment ).getP2pIps();
        doReturn( Sets.newHashSet(rhP2pIp) ).when( p2pIps ).getP2pIps();
    }


    @Test
    public void testExecute() throws Exception
    {
        step.execute();

        verify( peerUtil, times( 2 ) ).executeParallel();
    }
}
