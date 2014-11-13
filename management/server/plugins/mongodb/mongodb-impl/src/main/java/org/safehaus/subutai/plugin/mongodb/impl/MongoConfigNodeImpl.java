package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;


public class MongoConfigNodeImpl extends ContainerHost implements MongoConfigNode
{
    public MongoConfigNodeImpl( final Agent agent, final UUID peerId, final UUID environmentId )
    {
        super( agent, peerId, environmentId );
    }
}
