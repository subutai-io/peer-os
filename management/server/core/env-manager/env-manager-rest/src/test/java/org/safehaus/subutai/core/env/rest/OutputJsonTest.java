package org.safehaus.subutai.core.env.rest;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.environment.NodeGroup;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.util.JsonUtil;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class OutputJsonTest
{

    @Test
    public void testOutput() throws Exception
    {
        TopologyJson topology = new TopologyJson();

        topology.setEnvironmentName( "My environment" );

        Map<UUID, Set<NodeGroup>> placement = Maps.newHashMap();

        NodeGroup nodeGroup =
                new NodeGroup( "Node Group 1", "hadoop", 4, 1, 1, new PlacementStrategy( "ROUND_ROBIN" ) );
        NodeGroup nodeGroup2 = new NodeGroup( "Node Group 2", "cassandra", 4, 1, 1,
                new PlacementStrategy( "BEST_SERVER", new Criteria<>( "MORE_HDD", true ) ) );
        Set<NodeGroup> nodeGroups1 = Sets.newHashSet( nodeGroup, nodeGroup2 );
        NodeGroup nodeGroup3 =
                new NodeGroup( "Node Group 3", "master", 4, 1, 1, new PlacementStrategy( "ROUND_ROBIN" ) );
        Set<NodeGroup> nodeGroups2 = Sets.newHashSet( nodeGroup3 );

        placement.put( UUID.randomUUID(), nodeGroups1 );
        placement.put( UUID.randomUUID(), nodeGroups2 );

        topology.setNodeGroupPlacement( placement );

        System.out.println( JsonUtil.to( topology ) );
    }
}
