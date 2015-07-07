package io.subutai.core.env.rest;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.NodeGroup;

import io.subutai.core.env.rest.TopologyJson;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class TopologyJsonTest
{

    @Mock
    Map<UUID, Set<NodeGroup>> placement;

    TopologyJson topologyJson;


    @Before
    public void setUp() throws Exception
    {
        topologyJson = new TopologyJson();
    }


    @Test
    public void testProperties() throws Exception
    {
        topologyJson.setNodeGroupPlacement( placement );

        assertEquals( placement, topologyJson.getNodeGroupPlacement() );
    }
}
