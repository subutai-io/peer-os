package io.subutai.common.environment;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.protocol.PlacementStrategy;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class NodeGroupTest
{
    private NodeGroup nodeGroup;

    @Mock
    PlacementStrategy placementStrategy;


    @Before
    public void setUp() throws Exception
    {
        nodeGroup = new NodeGroup( "test", "testTeplate", 5, 5, 5, placementStrategy );
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
    }
}