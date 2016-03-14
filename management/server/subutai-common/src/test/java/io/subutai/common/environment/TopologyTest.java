package io.subutai.common.environment;


import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
@Ignore
public class TopologyTest
{
    private static final String ENVIRONMENT_ID = UUID.randomUUID().toString();
    private Topology topology;

    @Mock
    String peer;
    @Mock
    Node node;


    @Before
    public void setUp() throws Exception
    {
        topology = new Topology( "Name-" + ENVIRONMENT_ID/*, 0, 0 */);
    }


    @Test
    public void testGetNodeGroupPlacement() throws Exception
    {
        topology.getNodeGroupPlacement();
    }


    @Test
    public void testAddNodeGroupPlacement() throws Exception
    {
        topology.addNodePlacement( peer, node );
    }
}