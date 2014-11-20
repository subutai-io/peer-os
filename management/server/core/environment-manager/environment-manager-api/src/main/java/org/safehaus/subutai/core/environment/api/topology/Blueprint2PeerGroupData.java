package org.safehaus.subutai.core.environment.api.topology;


import java.util.UUID;


/**
 * Created by bahadyr on 11/6/14.
 */
public class Blueprint2PeerGroupData extends TopologyData
{

    final UUID blueprintId;
    final UUID peerGroupId;


    public Blueprint2PeerGroupData( final UUID blueprintId, final UUID peerGroupId )
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
}
