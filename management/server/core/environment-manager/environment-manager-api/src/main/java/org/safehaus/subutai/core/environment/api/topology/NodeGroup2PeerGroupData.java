package org.safehaus.subutai.core.environment.api.topology;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.NodeGroup;


public class NodeGroup2PeerGroupData extends TopologyData
{
    final UUID blueprintId;
    final UUID peerGroupId;
    final Map<NodeGroup, UUID> nodeGroupToPeer = new HashMap<>();


    public NodeGroup2PeerGroupData( UUID blueprintId, UUID peerGroupId )
    {
        this.blueprintId = blueprintId;
        this.peerGroupId = peerGroupId;
    }


    public UUID getBlueprintId()
    {
        return blueprintId;
    }


    public UUID getPeerGroupId()
    {
        return peerGroupId;
    }


    public Map<NodeGroup, UUID> getNodeGroupToPeer()
    {
        return nodeGroupToPeer;
    }


    public void setNodeGroupToPeer( Map<NodeGroup, UUID> nodeGroupToPeer )
    {
        this.nodeGroupToPeer.clear();
        this.nodeGroupToPeer.putAll( nodeGroupToPeer );
    }
}

