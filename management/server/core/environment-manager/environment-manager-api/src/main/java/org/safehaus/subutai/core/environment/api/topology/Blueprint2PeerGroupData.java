package org.safehaus.subutai.core.environment.api.topology;


import java.util.UUID;


public class Blueprint2PeerGroupData extends TopologyData
{

    final UUID peerGroupId;


    public Blueprint2PeerGroupData( final UUID blueprintId, final UUID peerGroupId )
    {
        this.blueprintId = blueprintId;
        this.peerGroupId = peerGroupId;
    }


    public UUID getPeerGroupId()
    {
        return peerGroupId;
    }
}

