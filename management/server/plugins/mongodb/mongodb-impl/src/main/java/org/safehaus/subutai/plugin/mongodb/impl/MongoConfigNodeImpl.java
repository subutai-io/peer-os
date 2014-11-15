package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;


public class MongoConfigNodeImpl extends MongoNodeImpl implements MongoConfigNode
{

    public MongoConfigNodeImpl( final Agent agent, final UUID peerId, final UUID environmentId, final String domainName,
                                final int port )
    {
        super( agent, peerId, environmentId, domainName, port );
    }
}
