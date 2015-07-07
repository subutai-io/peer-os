package io.subutai.common.peer;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.PeerPolicy;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class PeerPolicyTest
{
    private PeerPolicy peerPolicy;


    @Before
    public void setUp() throws Exception
    {
        peerPolicy = new PeerPolicy( UUID.randomUUID() );
        peerPolicy.setContainerCountLimit( -1 );
        peerPolicy.setCpuUsagePercentageLimit( 150 );
        peerPolicy.setDiskUsagePercentageLimit( 150 );
        peerPolicy.setMemoryUsagePercentageLimit( 150 );
        peerPolicy.setNetworkUsagePercentageLimit( 150 );
        peerPolicy.setEnvironmentCountLimit( -1 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull(peerPolicy.getContainerCountLimit());
        assertNotNull(peerPolicy.getCpuUsagePercentageLimit());
        assertNotNull(peerPolicy.getDiskUsagePercentageLimit());
        assertNotNull(peerPolicy.getMemoryUsagePercentageLimit());
        assertNotNull(peerPolicy.getNetworkUsagePercentageLimit());
        assertNotNull(peerPolicy.getEnvironmentCountLimit());
        assertNotNull(peerPolicy.getRemotePeerId());
        peerPolicy.hashCode();
        peerPolicy.equals( "test" );
        peerPolicy.equals( peerPolicy );

    }

    @Test
    public void test()
    {
        peerPolicy.setCpuUsagePercentageLimit( -1 );
        peerPolicy.setDiskUsagePercentageLimit( -1 );
        peerPolicy.setMemoryUsagePercentageLimit( -1 );
        peerPolicy.setNetworkUsagePercentageLimit( -1 );
    }
}