package org.safehaus.subutai.core.env.rest;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.environment.NodeGroup;


public class TopologyJson
{
    private String environmentName;
    private Map<UUID, Set<NodeGroup>> nodeGroupPlacement;


    public String getEnvironmentName()
    {
        return environmentName;
    }


    public void setEnvironmentName( final String environmentName )
    {
        this.environmentName = environmentName;
    }


    public Map<UUID, Set<NodeGroup>> getNodeGroupPlacement()
    {
        return nodeGroupPlacement;
    }


    public void setNodeGroupPlacement( final Map<UUID, Set<NodeGroup>> nodeGroupPlacement )
    {
        this.nodeGroupPlacement = nodeGroupPlacement;
    }
}
