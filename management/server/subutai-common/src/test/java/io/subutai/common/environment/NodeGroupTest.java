package io.subutai.common.environment;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.protocol.PlacementStrategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class NodeGroupTest
{
    private static final String PEER_ID = "peer_id";
    private static final String TEMPLATE_NAME = "template_name";
    private static final String NODEGROUP_NAME = "test_node_group";
    private NodeGroup nodeGroup;

    @Mock
    PlacementStrategy placementStrategy;


    @Before
    public void setUp() throws Exception
    {
        nodeGroup = new NodeGroup( NODEGROUP_NAME, TEMPLATE_NAME, 5, 5, 5, placementStrategy, PEER_ID );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( nodeGroup.getNumberOfContainers() );
        assertNotNull( nodeGroup.getContainerPlacementStrategy() );
        assertNotNull( nodeGroup.getHostsGroupId() );
        assertNotNull( nodeGroup.getName() );
        assertNotNull( nodeGroup.getSshGroupId() );
        assertNotNull( nodeGroup.getTemplateName() );
        assertNotNull( nodeGroup.getPeerId() );
        assertEquals( nodeGroup.getName(), NODEGROUP_NAME );
        assertEquals( nodeGroup.getPeerId(), PEER_ID );
        assertEquals( nodeGroup.getTemplateName(), TEMPLATE_NAME );
    }
}