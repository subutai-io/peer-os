package io.subutai.common.peer;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class PeerPolicyTest
{
    final String PEER_ID = UUID.randomUUID().toString();
    private PeerPolicy peerPolicy;


    @Before
    public void setUp() throws Exception
    {
        peerPolicy = new PeerPolicy( PEER_ID, 100, 100, 100, 100, 1, 1 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( 100, peerPolicy.getCpuUsageLimit() );
        assertEquals( 100, peerPolicy.getDiskUsageLimit() );
        assertEquals( 100, peerPolicy.getMemoryUsageLimit() );
        assertEquals( 100, peerPolicy.getNetworkUsageLimit() );
        assertEquals( 1, peerPolicy.getEnvironmentLimit() );
        assertEquals( 1, peerPolicy.getContainerLimit() );
        assertEquals( PEER_ID, peerPolicy.getPeerId() );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testInvalidCpuUsage()
    {
        peerPolicy.setCpuUsageLimit( -1 );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testInvalidMemoryUsage()
    {
        peerPolicy.setMemoryUsageLimit( -1 );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testInvalidDiskUsage()
    {
        peerPolicy.setDiskUsageLimit( -1 );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testInvalidNetworkUsage()
    {
        peerPolicy.setNetworkUsageLimit( -1 );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testInvalidEnvironmentLimit()
    {
        peerPolicy.setEnvironmentLimit( -1 );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testInvalidContainerLimit()
    {
        peerPolicy.setContainerLimit( -1 );
    }
}