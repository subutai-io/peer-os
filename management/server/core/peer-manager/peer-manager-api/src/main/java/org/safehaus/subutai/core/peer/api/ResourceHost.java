package org.safehaus.subutai.core.peer.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.core.strategy.api.ServerMetric;


/**
 * Management host interface.
 */
public interface ResourceHost extends Host
{
    public boolean startContainerHost( final ContainerHost container ) throws CommandException;

    public boolean stopContainerHost( final ContainerHost container ) throws CommandException;

    public ContainerHost getContainerHostByName( String hostname );

    public Set<ContainerHost> getContainerHostsByEnvironmentId( UUID environmentId );

    public void addContainerHost( ContainerHost containerHost );

    public ServerMetric getMetric() throws CommandException;

    public Set<ContainerHost> getContainerHosts();
}
