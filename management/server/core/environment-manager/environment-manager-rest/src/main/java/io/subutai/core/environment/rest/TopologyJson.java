package io.subutai.core.environment.rest;


import java.util.Map;
import java.util.Set;

import io.subutai.common.environment.NodeGroup;


public class TopologyJson
{

    private Map<String, Set<NodeGroup>> nodeGroupPlacement;


    public Map<String, Set<NodeGroup>> getNodeGroupPlacement()
    {
        return nodeGroupPlacement;
    }


    public void setNodeGroupPlacement( final Map<String, Set<NodeGroup>> nodeGroupPlacement )
    {
        this.nodeGroupPlacement = nodeGroupPlacement;
    }
}
