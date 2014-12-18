package org.safehaus.subutai.core.environment.api.topology;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.peer.api.Peer;


public class Node2PeerData extends TopologyData
{

    Map<Integer, Peer> topology;
    Map<Integer, NodeGroup> map;


    public Node2PeerData( final UUID blueprintId, final Map<Integer, Peer> topology, final Map<Integer, NodeGroup> map )
    {
        this.blueprintId = blueprintId;
        this.topology = topology;
        this.map = map;
    }


    public Map<Integer, Peer> getTopology()
    {
        return topology;
    }


    public void setTopology( final Map<Integer, Peer> topology )
    {
        this.topology = topology;
    }


    public Map<Integer, NodeGroup> getMap()
    {
        return map;
    }


    public void setMap( final Map<Integer, NodeGroup> map )
    {
        this.map = map;
    }
}

