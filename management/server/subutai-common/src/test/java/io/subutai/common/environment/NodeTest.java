package io.subutai.common.environment;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
@Ignore
public class NodeTest
{
    private static final String PEER_ID = "peer_id";
    private static final String TEMPLATE_NAME = "template_name";
    private static final String NODEGROUP_NAME = "test_node_group";
    private Node node;

    @Before
    public void setUp() throws Exception
    {
//        nodeGroup = new NodeGroup( NODEGROUP_NAME, TEMPLATE_NAME, 5, 5,/* 5, placementStrategy, */PEER_ID );
    }


    @Test
    public void testProperties() throws Exception
    {
//        assertNotNull( nodeGroup.getNumberOfContainers() );
//        assertNotNull( nodeGroup.getContainerPlacementStrategy() );
        assertNotNull( node.getHostsGroupId() );
        assertNotNull( node.getName() );
        assertNotNull( node.getSshGroupId() );
        assertNotNull( node.getTemplateName() );
        assertNotNull( node.getPeerId() );
        assertEquals( node.getName(), NODEGROUP_NAME );
        assertEquals( node.getPeerId(), PEER_ID );
        assertEquals( node.getTemplateName(), TEMPLATE_NAME );
    }
}