package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.Topology;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.EnvironmentPeerImpl;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ReservationStepTest
{

    ReservationStep step;
    @Mock
    Topology topology;
    EnvironmentImpl environment = TestHelper.ENVIRONMENT();
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
        doReturn( Sets.newHashSet( peer, TestHelper.PEER() ) ).when( peerManager ).resolve( anySet() );
    }


    @Test( expected = EnvironmentModificationException.class )
    public void testExecute() throws Exception
    {
        doReturn( 123 ).when( peerTaskResult ).getResult();
        doReturn( true ).when( peerTaskResult ).hasSucceeded();

        step.execute();

        verify( environment ).addEnvironmentPeer( isA( EnvironmentPeerImpl.class ) );

        doReturn( false ).when( peerTaskResult ).hasSucceeded();
        doReturn( true ).when( peerTaskResults ).hasFailures();

        step.execute();
    }


    @Test
    public void testCheckResourceAvailablility() throws Exception
    {
        Map<Peer, UsedNetworkResources> map = Maps.newHashMap();
        UsedNetworkResources resources = mock( UsedNetworkResources.class );
        map.put( TestHelper.PEER(), resources );

        step.checkResourceAvailablility( map, TestHelper.SUBNET_CIDR );

        verify( resources ).vniExists( anyLong() );
    }
}
