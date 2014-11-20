package org.safehaus.subutai.core.environment.api.topology;


import java.util.UUID;


/**
 * Created by bahadyr on 11/6/14.
 */
public class Blueprint2PeerData extends TopologyData
{

    final UUID blueprintId;
    final UUID peerId;


    public Blueprint2PeerData( final UUID peerId, final UUID blueprintId )
    {
        this.peerId = peerId;
        this.blueprintId = blueprintId;
    }


    public UUID getBlueprintId()
    {
        return blueprintId;
    }


    public UUID getPeerId()
    {
        return peerId;
    }
}
