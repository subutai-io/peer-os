package org.safehaus.subutai.core.peer.impl.container;


import java.util.Set;

import org.safehaus.subutai.core.peer.api.ContainerHost;


/**
 * Create container response
 */
public class CreateContainerResponse
{
    private Set<ContainerHost> containerHosts;


    public CreateContainerResponse( final Set<ContainerHost> containerHosts )
    {
        this.containerHosts = containerHosts;
    }


    public Set<ContainerHost> getContainerHosts()
    {
        return containerHosts;
    }
}

