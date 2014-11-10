package org.safehaus.subutai.plugin.mongodb.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public class MongoNode extends ContainerHost
{
    public MongoNode( final Agent agent, final UUID peerId, final UUID environmentId )
    {
        super( agent, peerId, environmentId );
    }
}
