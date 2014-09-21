package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Container;


/**
 * Created by timur on 9/20/14.
 */
public class PeerContainer extends Container
{
    // UUID of physical agent
    private UUID parentHostId;


    public UUID getParentHostId()
    {
        return parentHostId;
    }


    public void setParentHostId( final UUID parentHostId )
    {
        this.parentHostId = parentHostId;
    }
}
