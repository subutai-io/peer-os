package org.safehaus.subutai.core.environment.api.topology;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.peer.api.Peer;


/**
 * Created by bahadyr on 11/6/14.
 */
public class Node2PeerData extends TopologyData
{

    UUID blueprintId;
    Map<Object, Peer> topology;
    Map<Object, NodeGroup> map;


    public Node2PeerData( final UUID blueprintId, final Map<Object, Peer> topology, final Map<Object, NodeGroup> map )
    {
        this.blueprintId = blueprintId;
        this.topology = topology;
        this.map = map;
    }


    public UUID getBlueprintId()
    {
        return blueprintId;
    }


    public void setBlueprintId( final UUID blueprintId )
    {
        this.blueprintId = blueprintId;
    }


    public Map<Object, Peer> getTopology()
    {
        return topology;
    }


    public void setTopology( final Map<Object, Peer> topology )
    {
        this.topology = topology;
    }


    public Map<Object, NodeGroup> getMap()
    {
        return map;
    }


    public void setMap( final Map<Object, NodeGroup> map )
    {
        this.map = map;
    }
}
