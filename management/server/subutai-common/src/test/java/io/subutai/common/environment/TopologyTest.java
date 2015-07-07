package io.subutai.common.environment;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;


@RunWith( MockitoJUnitRunner.class )
public class TopologyTest
{
    private Topology topology;

    @Mock
    Peer peer;
    @Mock
    NodeGroup nodeGroup;

    @Before
    public void setUp() throws Exception
    {
        topology = new Topology();
    }


    @Test
    public void testGetNodeGroupPlacement() throws Exception
    {
        topology.getNodeGroupPlacement();
    }


    @Test
    public void testAddNodeGroupPlacement() throws Exception
    {
        topology.addNodeGroupPlacement( peer, nodeGroup );
    }
}