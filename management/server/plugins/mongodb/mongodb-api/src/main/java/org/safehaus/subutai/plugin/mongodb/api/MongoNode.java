package org.safehaus.subutai.plugin.mongodb.api;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.Host;


public interface MongoNode extends Host
{
    public UUID getEnvironmentId();

    public String getDomainName();

    public boolean isRunning();

    int getPort();

    void start() throws MongoException;

    void stop() throws MongoException;
}
