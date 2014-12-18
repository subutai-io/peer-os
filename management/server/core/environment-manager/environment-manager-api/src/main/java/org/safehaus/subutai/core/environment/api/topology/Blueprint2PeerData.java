package org.safehaus.subutai.core.environment.api.topology;


import java.util.UUID;


public class Blueprint2PeerData extends TopologyData
{

    final UUID peerId;


    public Blueprint2PeerData( final UUID peerId, final UUID blueprintId )
    {
        this.peerId = peerId;
        this.blueprintId = blueprintId;
    }


    public UUID getPeerId()
    {
        return peerId;
    }
}

