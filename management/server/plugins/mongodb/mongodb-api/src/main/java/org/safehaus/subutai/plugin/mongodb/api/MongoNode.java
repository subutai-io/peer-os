package org.safehaus.subutai.plugin.mongodb.api;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.Host;


public interface MongoNode extends Host
{
    public UUID getEnvironmentId();

    public boolean isRunning();

    int getPort();
}
