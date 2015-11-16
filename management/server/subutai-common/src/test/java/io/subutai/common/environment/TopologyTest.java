package io.subutai.common.environment;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.Peer;


@RunWith( MockitoJUnitRunner.class )
public class TopologyTest
{
    private static final String ENVIRONMENT_ID = UUID.randomUUID().toString();
    private Topology topology;

    @Mock
    Peer peer;
    @Mock
    NodeGroup nodeGroup;


    @Before
    public void setUp() throws Exception
    {
        topology = new Topology( "Name-" + ENVIRONMENT_ID, ENVIRONMENT_ID, null, null );
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