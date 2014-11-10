package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public class MongoConfigNodeImpl extends ContainerHost implements MongoNode
{
    public MongoConfigNodeImpl( final Agent agent, final UUID peerId, final UUID environmentId )
    {
        super( agent, peerId, environmentId );
    }
}
