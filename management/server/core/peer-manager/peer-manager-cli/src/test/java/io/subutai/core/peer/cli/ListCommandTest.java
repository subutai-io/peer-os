package io.subutai.core.peer.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.host.HostInfo;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.peer.api.PeerManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ListCommandTest extends SystemOutRedirectTest
{
    private static final String PEER_ID = UUID.randomUUID().toString();
    private static final String ERR_MSG = "error";
    private static final String HOST_NAME = "host_name";
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String CPU_MODEL = "Intel Core 5";

    @Mock
    PeerManager peerManager;
    @Mock
    Peer peer;
    @Mock
    PeerInfo peerInfo;

    @Mock
    ResourceHostMetric resourceHostMetric;
    @Mock
    ResourceHostMetrics resourceHostMetrics;

    @Mock
    HostInfo hostInfo;
    ListCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ListCommand();
        command.setPeerManager( peerManager );
        when( peerManager.getPeers() ).thenReturn( Lists.newArrayList( peer ) );
        when( peer.isOnline() ).thenReturn( true );
        when( peer.getId() ).thenReturn( PEER_ID );
        when( peer.getPeerInfo() ).thenReturn( peerInfo );
        when( resourceHostMetrics.getResources() ).thenReturn( Sets.newHashSet( resourceHostMetric ) );
        when( peer.getResourceHostMetrics() ).thenReturn( resourceHostMetrics );
        when( hostInfo.getHostname() ).thenReturn( HOST_NAME );
        when( resourceHostMetric.getHostInfo() ).thenReturn( hostInfo );
        when( resourceHostMetric.getContainersCount() ).thenReturn( 1 );
        when( resourceHostMetric.getCpuModel() ).thenReturn( CPU_MODEL );
        when( hostInfo.getId() ).thenReturn( HOST_ID );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        assertTrue( getSysOut().contains( PEER_ID.toString() ) );

        doThrow( new PeerException( ERR_MSG ) ).when( peer ).isOnline();

        command.doExecute();

        assertTrue( getSysOut().contains( ERR_MSG ) );
    }
}
