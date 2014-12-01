package org.safehaus.subutai.plugin.mongodb.api;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public interface MongoNode
{
    //    public UUID getEnvironmentId();

    public String getDomainName();

    public boolean isRunning();

    int getPort();

    void start() throws MongoException;

    void stop() throws MongoException;

    String getHostname();

    CommandResult execute( RequestBuilder build ) throws CommandException;

    UUID getPeerId();

    ContainerHost getContainerHost();
}
